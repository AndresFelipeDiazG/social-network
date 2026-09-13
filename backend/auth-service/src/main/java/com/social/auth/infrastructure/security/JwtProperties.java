package com.social.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "social.jwt")
public record JwtProperties(String secret, String issuer, Duration expiration) {

    private static final int MINIMUM_SECRET_BYTES = 32;

    // Una clave corta hace HS256 atacable por fuerza bruta, y Nimbus falla despues
    // con un error que no menciona la causa.
    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "social.jwt.secret debe tener al menos " + MINIMUM_SECRET_BYTES
                            + " bytes. Generar con: openssl rand -base64 48");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("social.jwt.issuer es obligatorio");
        }
        if (expiration == null || expiration.isNegative() || expiration.isZero()) {
            throw new IllegalArgumentException("social.jwt.expiration debe ser una duracion positiva");
        }
    }
}
