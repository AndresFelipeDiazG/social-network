package com.social.auth.domain.model;

import java.util.UUID;

public record User(UUID id, String username, String displayName, PasswordHash passwordHash) {

    public User {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio");
        }
        if (passwordHash == null) {
            throw new IllegalArgumentException("El hash de la contrasena es obligatorio");
        }
        requireText(username, "username");
        requireText(displayName, "displayName");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El campo " + field + " es obligatorio");
        }
    }
}
