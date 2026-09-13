-- Un me gusta es un hecho que existe o no existe, no un contador. La clave
-- primaria compuesta es la que garantiza que un usuario no pueda marcar dos
-- veces la misma publicacion: la idempotencia la impone el esquema, no el codigo.
CREATE TABLE post_likes (
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
CREATE INDEX post_likes_user_idx ON post_likes (user_id);
