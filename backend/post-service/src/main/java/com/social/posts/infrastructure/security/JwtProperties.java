package com.social.posts.infrastructure.security;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * No declara duracion de expiracion, a diferencia de la de auth-service: este
 * servicio verifica tokens y no los emite, asi que no tiene nada que decir sobre
 * cuanto duran.
 */
@ConfigurationProperties(prefix = "social.jwt")
public record JwtProperties(String secret, String issuer) {

    private static final int MINIMUM_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "social.jwt.secret debe tener al menos " + MINIMUM_SECRET_BYTES
                            + " bytes y coincidir con el de auth-service");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("social.jwt.issuer es obligatorio");
        }
    }
}
