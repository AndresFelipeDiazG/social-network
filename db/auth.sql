-- Esquema y datos de prueba de la base de datos social_auth.
--
-- Vista consolidada de las migraciones de Flyway de auth-service:
--   V1__create_users_table.sql, V2__seed_users.sql
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
--   psql -h localhost -p 5433 -U auth_service -d social_auth -f auth.sql

CREATE TABLE IF NOT EXISTS users (
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


-- Usuarios de prueba, sembrados al arrancar la aplicacion.
--
-- Los identificadores son UUID fijos y coinciden con los que usa el seed de
-- post-service. Eso es lo que permite asociar publicaciones a usuarios sin que
-- los dos servicios compartan base de datos ni se consulten entre ellos.
--
-- La contrasena de los cuatro es Password123! (documentada en el README).
-- Los hashes son BCrypt con coste 10, generados con el mismo BCryptPasswordEncoder
-- que la aplicacion usa para verificarlos.
--
-- ON CONFLICT hace la insercion idempotente. Flyway ya garantiza que se ejecute
-- una sola vez, pero este archivo tambien se entrega como script suelto en db/ y
-- alguien puede aplicarlo sobre una base de datos que ya tenga filas.

INSERT INTO users (id, username, display_name, password_hash) VALUES
    ('11111111-1111-1111-1111-111111111111', 'acorrea',   'Ana Correa',
     '$2a$10$NEpssAucp9CBbxqg/hz0xelk2gFYH8vdZWrqOPj68SvEZktN3krsC'),

    ('22222222-2222-2222-2222-222222222222', 'jmendoza',  'Julian Mendoza',
     '$2a$10$r5mn9WWHVLSUOCxrMgzGr.qGhatU8CBLQLbyyHPJEvH98Vg1Wsn7K'),

    ('33333333-3333-3333-3333-333333333333', 'lvargas',   'Lucia Vargas',
     '$2a$10$sCgu/wIZNUST8qqEMXPW.e6Sjd9kGvewFi3ZZWPk8kx6DKtmzg7Ai'),

    ('44444444-4444-4444-4444-444444444444', 'dcastillo', 'Diego Castillo',
     '$2a$10$yJ0JbbBuVD6p6MTonq6vjOF2XqbxmCG11/ZCRQEmZjQAd7mcSS1Tm')

ON CONFLICT (id) DO NOTHING;
