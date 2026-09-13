# Red Social — Microservicios con Spring Boot y Angular

Red social minima con arquitectura de microservicios: autenticacion con JWT, muro
de publicaciones y creacion de mensajes.

---

## Arquitectura

```
                    ┌──────────────────────────────┐
   Navegador ──────▶│  frontend  (nginx :4200)     │
                    │  Angular 21 + SignalStore    │
                    │  ──────────────────────────  │
                    │  /       → estaticos         │
                    │  /api/*  → proxy inverso     │
                    └──────────────┬───────────────┘
                                   │
                    ┌──────────────▼───────────────┐
                    │  api-gateway  :8080          │
                    │  enrutado · CORS · rechazo   │
                    │  temprano de tokens          │
                    └───────┬──────────────┬───────┘
                            │              │
          /api/auth/**      │              │  /api/posts/**
                            │              │
              ┌─────────────▼────┐   ┌─────▼──────────────┐
              │  auth-service    │   │  post-service      │
              │  :8081           │   │  :8082             │
              │  emite el JWT    │   │  verifica el JWT   │
              └─────────┬────────┘   └─────┬──────────────┘
                        │                  │
              ┌─────────▼────────┐   ┌─────▼──────────────┐
              │  postgres-auth   │   │  postgres-posts    │
              │  social_auth     │   │  social_posts      │
              └──────────────────┘   └────────────────────┘
```

Una base de datos por servicio. `post-service` **no** consulta a `auth-service`
para validar tokens: verifica la firma localmente con el mismo secreto, asi que
no hay salto de red ni dependencia en tiempo de ejecucion entre ellos.

---

## Requisitos

| | Version | Comprobar |
|---|---|---|
| Docker | 24+ | `docker --version` |
| Docker Compose | v2+ | `docker compose version` |

Nada mas. Java, Maven y Node solo son necesarios para desarrollar fuera de
contenedores.

---

## Arranque

```bash
cp .env.example .env
```

Rellena `JWT_SECRET` y las dos contrasenas; el propio archivo indica los
comandos para generarlas. Despues:

```bash
docker compose up -d --build
```

Si falta el `.env`, Compose aborta indicando exactamente que hacer, en lugar de
arrancar con credenciales vacias.

### URLs

| | URL |
|---|---|
| Aplicacion | http://localhost:4200 |
| **Swagger (API completa)** | **http://localhost:8080/swagger-ui.html** |
| Swagger · auth-service | http://localhost:8081/swagger-ui.html |
| Swagger · post-service | http://localhost:8082/swagger-ui.html |

### Usuarios de prueba

Los crea Flyway al arrancar la aplicacion, con una publicacion cada uno.

| Usuario | Nombre | Contrasena |
|---|---|---|
| `acorrea` | Ana Correa | `Password123!` |
| `jmendoza` | Julian Mendoza | `Password123!` |
| `lvargas` | Lucia Vargas | `Password123!` |
| `dcastillo` | Diego Castillo | `Password123!` |

---

## API

| Metodo | Ruta | Descripcion |
|---|---|---|
| `POST` | `/api/auth/login` | Login con credenciales en el cuerpo. Devuelve el JWT. |
| `GET` | `/api/auth/login` | Login con cabecera `Authorization: Basic`. La que usa el frontend. |
| `GET` | `/api/auth/me` | Usuario correspondiente al token actual. |
| `GET` | `/api/posts` | Lista publicaciones. Parametros: `scope` (ALL, OTHERS, MINE), `page`, `size`. |
| `POST` | `/api/posts` | Crea una publicacion. La fecha por defecto es el instante de guardado. |
| `PUT` | `/api/posts/{id}/like` | Marca la publicacion con me gusta. Idempotente. |
| `DELETE` | `/api/posts/{id}/like` | Retira el me gusta. Tambien idempotente. |
| `POST` | `/api/posts/images` | Sube una imagen (multipart, 2 MB) y devuelve su identificador. |
| `GET` | `/api/posts/images/{id}` | Devuelve los bytes de la imagen. Publica. |

Comprobacion de extremo a extremo:

```bash
./scripts/smoke-test.sh
```

---

## Frontend

Angular 21 con componentes autonomos, senales y deteccion de cambios sin Zone.js.

| Pantalla | Ruta | Contenido |
|---|---|---|
| Entrada | `/entrar` | Usuario y contrasena, con boton para revelarla. Lista los usuarios de prueba. |
| Muro | `/muro` | Publicaciones, compositor, filtros, buscador y carga incremental. |

Las dos se cargan en diferido: el arranque baja 66 kB comprimidos y la pantalla
que toque, no las dos.

El estado vive en dos `signalStore` de NgRx:

- **`AuthStore`**, en la raiz. Token, usuario y sesion persistida en
  `localStorage` con su instante de caducidad. Una sesion vencida se descarta al
  arrancar, sin esperar al primer 401.
- **`PostsStore`**, provisto en la pagina del muro. Publicaciones, filtro,
  pagina y errores. Se destruye con el componente, asi que al salir no queda el
  muro de la sesion anterior en memoria.

Decisiones que no se ven en una captura:

- El filtro por defecto es **de otros usuarios**, que es literalmente lo que pide
  el enunciado. `Todas` y `Mias` quedan como filtro explicito.
- El paginado es **incremental** (`Cargar mas publicaciones` con un contador
  `Mostrando X de Y`), no paginas numeradas: en un muro que se recorre hacia
  abajo, «ir a la pagina 3» no significa nada. Descarta repetidas por `id`,
  porque una publicacion nueva desplaza la ventana entre dos peticiones.
- El buscador filtra **sobre lo ya cargado**, y el texto lo dice. El backend no
  expone busqueda, y fingir que busca en todo el muro seria mentir en la interfaz.
- Al publicar desde el filtro `De otros` se pasa a `Todas`, porque la propia
  publicacion no cabe en ese filtro y el usuario espera verla.
- Tema claro, oscuro y del sistema. Los tokens son propiedades personalizadas de
  CSS: cambiar de tema no recompila nada y el modo «sistema» no escucha eventos,
  deja mandar a `prefers-color-scheme`.
- Dos columnas en pantalla ancha: el muro no llena una pantalla de 1440 px por
  si solo. La lateral se oculta por debajo de 1000 px y el muro se queda en 640,
  que es el ancho de linea comodo de leer.
- El me gusta se pinta antes de que responda el servidor y se deshace si falla.
  El endpoint es idempotente, asi que un reintento no descuadra el recuento.
- Las imagenes se suben en una peticion aparte que devuelve un identificador, y
  la publicacion se crea despues con el. Convertir `POST /api/posts` en multipart
  habria roto el contrato JSON que ya estaba probado.
- El compositor lleva una paleta de emojis que inserta donde este el cursor, no
  al final. Es una paleta corta y fija: un catalogo completo pide busqueda,
  categorias y varios cientos de kilobytes de datos.
- La barra superior solo muestra el avatar; cerrar sesion vive dentro del menu que
  se despliega al pulsarlo, no suelto al lado.
- Toda la interfaz usa Inter, servida desde el propio bundle y no desde una CDN:
  la aplicacion tiene que verse igual sin salida a internet.
- Los errores llegan en `ProblemDetail` (RFC 7807) y se muestran tal cual: el
  mensaje ya viene redactado desde el backend, en un solo idioma y un solo sitio.

---

## Pruebas

```bash
cd backend/auth-service

./mvnw test      # unitarias y de slice: rapidas, sin Docker
./mvnw verify    # ademas las de integracion: levantan un Postgres real
```

```bash
cd frontend && npm test    # Vitest, sin navegador
```

| Modulo | Pruebas |
|---|---|
| auth-service | 38 |
| post-service | 70 |
| api-gateway | 19 |
| frontend | 74 |
| **Total** | **201** |

La separacion es deliberada: `*Test.java` corre con surefire y no necesita
Docker, mientras `*IT.java` corre con failsafe y levanta Postgres con
Testcontainers. Mezclarlas obligaria a tener Docker para ejecutar cualquier
prueba, que es como acaba desactivandose una suite entera.

---

## Estructura

```
.
├── docker-compose.yml        orquestacion local
├── .env.example              plantilla de configuracion
├── backend/
│   ├── auth-service/         autenticacion y emision de JWT
│   ├── post-service/         publicaciones
│   └── api-gateway/          punto de entrada unico
├── frontend/                 Angular 21 + NgRx SignalStore
├── db/                       scripts SQL: esquema y usuarios de prueba
├── scripts/                  prueba de humo
└── docs/                     documentacion y diagramas
```

Cada servicio lleva su propio `Dockerfile` y su `.dockerignore`. No es
preferencia de estilo: Docker lee `.dockerignore` solo en la raiz del contexto de
build, y un `Dockerfile` no puede copiar nada de fuera de su contexto. Eso obliga
a que cada servicio se construya de forma autocontenida.

---

## Decisiones de arquitectura

### Una base de datos por servicio

Dos instancias de Postgres en lugar de una con dos esquemas. Con instancias
separadas, que un servicio consulte los datos del otro es **imposible**, no solo
esta mal visto. La frontera que no se puede cruzar por accidente vale mas que la
que depende de la disciplina del equipo.

Para un producto real pequeno, una instancia con dos bases de datos y un usuario
distinto por servicio es igual de defendible: menos coste operativo, y el
principio se respeta mientras cada servicio solo tenga permisos sobre lo suyo. Lo
que nunca es defendible es compartir usuario y esquema.

### El nombre del autor va desnormalizado en `posts`

`post-service` guarda `author_username` en lugar de consultar a `auth-service`.
No es un atajo: **una publicacion es un registro historico inmutable**. «Este
usuario dijo esto en este momento» — el nombre en el instante de publicar es
semanticamente correcto, igual que una factura guarda la direccion del cliente en
vez de referenciarla.

La alternativa, resolver los nombres llamando al otro servicio al leer, anade
acoplamiento en tiempo de ejecucion: si `auth-service` cae, el muro deja de
funcionar.

### Sin libreria compartida entre servicios

Un modulo comun reintroduce acoplamiento en tiempo de compilacion, que es
precisamente lo que los microservicios existen para eliminar: se cambia la
libreria y hay que recompilar, versionar y redesplegar de forma coordinada todo
lo que la consume.

Usando el soporte de *resource server* de Spring Security en lugar de escribir un
filtro JWT a mano, la cuestion desaparece: no hay logica criptografica que
compartir, solo un bean `JwtDecoder` de cuatro lineas por servicio.

### Autenticacion en el borde, verificacion en cada servicio

El gateway valida firma y expiracion para rechazar tokens invalidos antes de
consumir recursos aguas abajo. Pero **pasa la cabecera `Authorization` intacta y
no inyecta cabeceras de identidad**: cada servicio vuelve a verificar por su
cuenta.

El patron alternativo, en el que el gateway extrae la identidad y los servicios
confian en un `X-User-Id`, tiene un fallo concreto: cualquier cosa que alcance el
servicio directamente puede enviar esa cabecera y nadie la comprueba. Un puerto
mal publicado, un contenedor en la misma red, un SSRF.

Y hay un argumento de pruebas que lo cierra: si los servicios confian en
cabeceras, **no se puede escribir** una prueba de «token manipulado devuelve
401», porque esa propiedad no existe. Verificando en cada servicio, la prueba de
integracion recorre el mismo camino que produccion.

### HS256 con secreto compartido

Proporcionado para este alcance. La evolucion natural es RS256: `auth-service`
firma con la clave privada y los demas validan con la publica expuesta via JWKS,
de modo que el secreto no se comparte con nadie. Es un cambio de configuracion,
no de codigo.

Limitacion que conviene conocer: un JWT no se puede revocar antes de que expire.
Se mitiga con un TTL corto (aqui 60 minutos) y, si hiciera falta, una lista negra
en un almacen rapido.

### Login por `POST` y tambien por `GET`

El enunciado pide «login con JWT (GET)». Enviar credenciales en la URL de un GET
es mala practica: quedan en los registros del servidor, en el historial del
navegador y en la cabecera `Referer`.

La solucion cumple las dos cosas: `POST /api/auth/login` con las credenciales en
el cuerpo, que es el que usa el frontend, y `GET /api/auth/login` con
`Authorization: Basic`, que es un GET real sin credenciales en la URL.

### Flyway en lugar de `ddl-auto`

`spring.jpa.hibernate.ddl-auto=update` no es reproducible, no se puede revisar en
un pull request, no tiene vuelta atras y no se usa en produccion. Flyway aplica
migraciones versionadas con checksum, una sola vez por base de datos.

Resuelve tres entregables a la vez: el *seeder* al iniciar la aplicacion, el
script de base de datos con usuarios predefinidos, y un esquema reproducible.

### Testcontainers en lugar de H2

H2 no es Postgres: dialecto distinto, sin `gen_random_uuid()`, y los tipos `uuid`
y `timestamptz` se comportan de otra forma. Las migraciones de Flyway no
llegarian ni a ejecutarse. Probar contra una base de datos que no se despliega es
probar una ficcion.

### Configuracion del frontend en tiempo de ejecucion

Angular compila a JavaScript estatico, asi que no puede leer variables de
entorno. En lugar de generar `environment.ts` al compilar —lo que obligaria a una
imagen distinta por entorno— el contenedor genera `config.js` con `envsubst`
antes de arrancar nginx.

La consecuencia importa: **la imagen que construyes y verificas es, bit a bit,
la que llega a produccion**. Cambiar la URL de la API es reiniciar un contenedor, no
recompilar.

Todo lo que hay en `config.js` es publico: llega al navegador y se lee con F12.
Nunca un secreto.

### nginx como proxy inverso de `/api`

El frontend llama a `/api`, su propio origen, asi que no hay peticion cruzada y
**CORS deja de existir** en lugar de configurarse. En desarrollo,
`proxy.conf.json` reproduce lo mismo en `ng serve`: comportamiento identico en
ambos entornos, sin ramas en el codigo.

### Monorepo

El modelo de repositorio y el de despliegue son decisiones independientes:
**monorepo no es monolito**. Sale una imagen por servicio, y cada una se
despliega y escala por separado.

A esta escala, un repositorio por servicio anadiria coordinacion sin resolver
ningun problema real. Si el proyecto creciera, el mecanismo son tags con prefijo
(`auth-service/v1.2.0`) y construir unicamente lo que ha cambiado.

### Lombok con alcance limitado

Solo `@Getter` y `@Setter` en entidades JPA. **Nunca `@Data` sobre una
`@Entity`**: genera `equals` y `hashCode` con todos los campos mutables, de modo
que dos entidades iguales dejan de serlo al persistirse y recibir el ID —
rompiendo cualquier `Set` que las contenga — y el `toString` dispara la carga de
relaciones lazy.

Los DTO son `record` de Java 21, que no necesitan Lombok.

---

## Notas sobre Spring Boot 4

El proyecto usa Spring Boot **4.1.1**, que cambia cosas respecto a la
documentacion y los tutoriales de la linea 3.x:

| | Boot 3.x | Boot 4.x |
|---|---|---|
| Starter web | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| Starters de prueba | uno unico | modulares (`-webmvc-test`, `-data-jpa-test`…) |
| Mocks en pruebas | `@MockBean` | `@MockitoBean` |
| Swagger | springdoc 2.x | **springdoc 3.x** |
| Testcontainers Postgres | `org.testcontainers:postgresql` | `org.testcontainers:testcontainers-postgresql` |

Versiones gestionadas por el BOM: Spring Framework 7.0.9, Spring Security 7.1.1,
Hibernate 7.4.5, Flyway 12.4.0, Jackson 3.1.5, Testcontainers 2.0.5.

Un detalle poco documentado: **no existe** la propiedad
`spring.security.oauth2.resourceserver.jwt.secret-key`. La autoconfiguracion solo
cubre `issuer-uri`, `jwk-set-uri` y `public-key-location`, asi que para HS256
simetrico hay que declarar el bean `JwtDecoder` explicitamente.
