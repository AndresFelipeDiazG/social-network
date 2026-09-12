# Scripts de base de datos

Entregable: *«Script para la base de datos con usuarios predefinidos»*.

## Importante: estos archivos son una copia

La **fuente de verdad** son las migraciones de Flyway, que viven dentro de cada
servicio y son las que se ejecutan de verdad al arrancar la aplicacion:

```
backend/auth-service/src/main/resources/db/migration/
backend/post-service/src/main/resources/db/migration/
```

Flyway compara esas migraciones con la tabla `flyway_schema_history` de cada
base de datos y aplica lo que falte, una sola vez, guardando un checksum. Ese es
el mecanismo real de creacion del esquema y de siembra de datos.

Los archivos de esta carpeta son la **vista consolidada** de ese mismo contenido,
para inspeccion manual o para crear el esquema sin arrancar los servicios.

Es una duplicacion consciente: el enunciado pide un script suelto como
entregable, y la alternativa —no entregarlo— incumple el requisito. Se documenta
en lugar de disimularse. En un proyecto real solo existirian las migraciones.

## Estructura

| Archivo | Base de datos | Contenido |
|---|---|---|
| `auth.sql` | `social_auth` | tabla `users` y los usuarios de prueba |
| `posts.sql` | `social_posts` | tabla `posts` y una publicacion por usuario |

Los identificadores de usuario son UUID fijos, iguales en los dos scripts. Eso
es lo que permite que `post-service` asocie publicaciones a usuarios de
`auth-service` sin compartir base de datos ni consultarla.

## Uso manual

```bash
psql -h localhost -p 5433 -U auth_service -d social_auth  -f auth.sql
psql -h localhost -p 5434 -U post_service -d social_posts -f posts.sql
```

No hace falta para el arranque normal: `docker compose up` aplica las
migraciones automaticamente.
