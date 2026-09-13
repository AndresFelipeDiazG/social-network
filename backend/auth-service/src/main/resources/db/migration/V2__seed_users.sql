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
