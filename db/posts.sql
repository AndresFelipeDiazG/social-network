-- Esquema y datos de prueba de la base de datos social_posts.
--
-- Vista consolidada de las migraciones de Flyway de post-service:
--   V1__create_posts_table.sql, V2__seed_posts.sql,
--   V3__create_post_likes_table.sql, V4__create_post_images_table.sql
--
-- Esos archivos son la fuente de verdad y son los que se ejecutan al arrancar
-- docker compose. Este script existe porque el entregable pide un script suelto,
-- y sirve para crear el esquema sin levantar los servicios.
--
-- Diferencia deliberada con las migraciones: aqui las sentencias llevan
-- IF NOT EXISTS y ON CONFLICT DO NOTHING, de modo que se puede ejecutar dos
-- veces sin error. Flyway no lo necesita porque lleva su propio registro de lo
-- ya aplicado en la tabla flyway_schema_history.
--
--   psql -h localhost -p 5434 -U post_service -d social_posts -f posts.sql

CREATE TABLE IF NOT EXISTS posts (
    id                  UUID         PRIMARY KEY,
    message             VARCHAR(280) NOT NULL,
    author_id           UUID         NOT NULL,
    author_username     VARCHAR(50)  NOT NULL,
    author_display_name VARCHAR(100) NOT NULL,
    published_at        TIMESTAMPTZ  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT posts_message_not_blank CHECK (btrim(message) <> '')
);

-- No hay clave ajena hacia users y no es un descuido: esa tabla vive en la base de
-- datos de auth-service, y Postgres no puede referenciar entre bases distintas. Es
-- la consecuencia visible de tener una base de datos por servicio.
COMMENT ON COLUMN posts.author_id IS
    'Identificador del usuario en auth-service. Sin clave ajena: otra base de datos.';

-- El nombre del autor se copia al publicar en lugar de consultarse al leer. Una
-- publicacion es un registro historico: el nombre que tenia el autor en ese
-- momento es el dato correcto, igual que una factura guarda la direccion de
-- entonces y no la actual.
COMMENT ON COLUMN posts.author_display_name IS
    'Copia del nombre del autor en el momento de publicar.';

COMMENT ON COLUMN posts.created_at IS
    'Solo auditoria. La aplicacion no mapea esta columna: la fecha de negocio es published_at.';

-- El feed ordena siempre por published_at descendente. El desempate por id hace la
-- paginacion estable: sin el, dos publicaciones con la misma fecha pueden aparecer
-- en dos paginas distintas o en ninguna.
CREATE INDEX IF NOT EXISTS posts_feed_idx ON posts (published_at DESC, id);

-- Cubre los filtros por autor, que son los scope OTHERS y MINE.
CREATE INDEX IF NOT EXISTS posts_author_feed_idx ON posts (author_id, published_at DESC, id);


-- Una publicacion por usuario, sembrada al arrancar la aplicacion.
--
-- Los author_id son los mismos UUID fijos que siembra auth-service en su tabla
-- users. Esa coincidencia es el mecanismo completo que vincula publicaciones con
-- usuarios sin que los dos servicios compartan base de datos ni se consulten.
--
-- Las fechas son relativas a now() en lugar de literales, para que el muro se vea
-- reciente cada vez que se levanta el entorno. Estan escalonadas, asi que el orden
-- del feed es deterministico y comprobable.

INSERT INTO posts (id, message, author_id, author_username, author_display_name, published_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'Primera semana en el proyecto nuevo y ya tengo el entorno levantado. Buena senal.',
     '11111111-1111-1111-1111-111111111111', 'acorrea', 'Ana Correa',
     now() - interval '4 hours'),

    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'Dedique la manana a separar el dominio de la infraestructura. Las pruebas bajaron de 40 segundos a 2.',
     '22222222-2222-2222-2222-222222222222', 'jmendoza', 'Julian Mendoza',
     now() - interval '3 hours'),

    ('cccccccc-cccc-cccc-cccc-cccccccccccc',
     'Recordatorio para mi misma: si una prueba necesita cinco mocks, el problema no es la prueba.',
     '33333333-3333-3333-3333-333333333333', 'lvargas', 'Lucia Vargas',
     now() - interval '2 hours'),

    ('dddddddd-dddd-dddd-dddd-dddddddddddd',
     'Migraciones versionadas desde el primer dia. Nunca mas un esquema que solo existe en la maquina de alguien.',
     '44444444-4444-4444-4444-444444444444', 'dcastillo', 'Diego Castillo',
     now() - interval '1 hour')

ON CONFLICT (id) DO NOTHING;


-- Un me gusta es un hecho que existe o no existe, no un contador. La clave
-- primaria compuesta es la que garantiza que un usuario no pueda marcar dos
-- veces la misma publicacion: la idempotencia la impone el esquema, no el codigo.
CREATE TABLE IF NOT EXISTS post_likes (
    post_id    UUID        NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT post_likes_pk PRIMARY KEY (post_id, user_id)
);

-- Aqui si hay clave ajena hacia posts, porque las dos tablas viven en la misma
-- base de datos. Hacia el usuario no la hay: esa tabla es de auth-service.
COMMENT ON COLUMN post_likes.user_id IS
    'Identificador del usuario en auth-service. Sin clave ajena: otra base de datos.';

-- La clave primaria ya cubre las consultas por publicacion. Este indice cubre la
-- direccion contraria: que ha marcado un usuario.
CREATE INDEX IF NOT EXISTS post_likes_user_idx ON post_likes (user_id);


-- Los bytes de la imagen viven en la base de datos y no en un volumen. Es una
-- decision de entrega, no de arquitectura: asi el proyecto levanta con un solo
-- comando y queda limpio con «docker compose down -v», sin rutas montadas ni
-- permisos que ajustar. Con volumen de imagenes reales, lo correcto seria un
-- almacen de objetos y guardar aqui solo la referencia.
CREATE TABLE IF NOT EXISTS post_images (
    id           UUID        PRIMARY KEY,
    content_type VARCHAR(40) NOT NULL,
    size_bytes   INTEGER     NOT NULL,
    bytes        BYTEA       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT post_images_size_limit CHECK (size_bytes > 0 AND size_bytes <= 2097152)
);

COMMENT ON COLUMN post_images.content_type IS
    'Deducido de los bytes iniciales, no de la cabecera que envia el cliente.';

-- La clave ajena impide que una publicacion apunte a una imagen inexistente.
-- Sin ella habria que comprobarlo en la aplicacion, con una ventana entre la
-- comprobacion y la insercion.
ALTER TABLE posts ADD COLUMN IF NOT EXISTS image_id UUID REFERENCES post_images (id);
