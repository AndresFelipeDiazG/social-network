package com.social.posts.domain.usecase;

import com.social.posts.domain.exception.InvalidImageException;
import com.social.posts.domain.model.ImageFormat;
import com.social.posts.domain.port.PostImages;
import java.util.UUID;

public class StoreImage {

    /** Dos megabytes bastan para una foto de muro y acotan lo que cabe en la fila. */
    public static final int MAX_BYTES = 2 * 1024 * 1024;

    private final PostImages images;

    public StoreImage(PostImages images) {
        this.images = images;
    }

    public UUID store(byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidImageException("La imagen esta vacia");
        }
        if (content.length > MAX_BYTES) {
            throw new InvalidImageException("La imagen no puede superar los 2 MB");
        }

        // El tipo sale de los bytes, no de lo que declare el cliente.
        ImageFormat format = ImageFormat.of(content);
        if (format == null) {
            throw new InvalidImageException("Solo se admiten imagenes JPEG, PNG, GIF o WEBP");
        }

        return images.save(format.contentType(), content);
    }
}
