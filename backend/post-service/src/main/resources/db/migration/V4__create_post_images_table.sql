-- Los bytes de la imagen viven en la base de datos y no en un volumen. Es una
-- decision de entrega, no de arquitectura: asi el proyecto levanta con un solo
-- comando y queda limpio con «docker compose down -v», sin rutas montadas ni
-- permisos que ajustar. Con volumen de imagenes reales, lo correcto seria un
-- almacen de objetos y guardar aqui solo la referencia.
CREATE TABLE post_images (
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
ALTER TABLE posts ADD COLUMN image_id UUID REFERENCES post_images (id);
