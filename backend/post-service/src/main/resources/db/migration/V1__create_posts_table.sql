CREATE TABLE posts (
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
CREATE INDEX posts_feed_idx ON posts (published_at DESC, id);

-- Cubre los filtros por autor, que son los scope OTHERS y MINE.
CREATE INDEX posts_author_feed_idx ON posts (author_id, published_at DESC, id);
