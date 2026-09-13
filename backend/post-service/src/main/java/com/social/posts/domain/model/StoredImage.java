package com.social.posts.domain.model;

import java.util.UUID;

public record StoredImage(UUID id, String contentType, byte[] bytes) {

    public StoredImage {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la imagen es obligatorio");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("El tipo de la imagen es obligatorio");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("La imagen esta vacia");
        }
    }
}
