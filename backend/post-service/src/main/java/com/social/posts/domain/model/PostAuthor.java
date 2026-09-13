package com.social.posts.domain.model;

import java.util.UUID;

public record PostAuthor(UUID id, String username, String displayName) {

    public PostAuthor {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del autor es obligatorio");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario del autor es obligatorio");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("El nombre del autor es obligatorio");
        }
    }
}
