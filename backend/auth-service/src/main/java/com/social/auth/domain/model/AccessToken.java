package com.social.auth.domain.model;

import java.time.Duration;
import java.time.Instant;

public record AccessToken(String value, Instant issuedAt, Instant expiresAt) {

    public AccessToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El token no puede estar vacio");
        }
        if (issuedAt == null || expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("La expiracion debe ser posterior a la emision");
        }
    }

    public long secondsUntilExpiry() {
        return Duration.between(issuedAt, expiresAt).toSeconds();
    }
}
