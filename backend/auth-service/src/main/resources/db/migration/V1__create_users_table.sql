CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT users_username_unique UNIQUE (username),

    -- La aplicacion normaliza el usuario a minusculas antes de consultar. Esta
    -- restriccion impide que una fila insertada por otra via (una carga manual,
    -- otro script) quede inaccesible para el login por diferir en mayusculas.
    CONSTRAINT users_username_lowercase CHECK (username = lower(username)),

    CONSTRAINT users_username_not_blank CHECK (btrim(username) <> ''),
    CONSTRAINT users_display_name_not_blank CHECK (btrim(display_name) <> '')
);

COMMENT ON TABLE users IS 'Usuarios que pueden autenticarse. Propiedad exclusiva de auth-service.';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt. Nunca la contrasena en claro.';
