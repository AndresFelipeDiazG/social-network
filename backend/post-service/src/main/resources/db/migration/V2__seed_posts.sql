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
