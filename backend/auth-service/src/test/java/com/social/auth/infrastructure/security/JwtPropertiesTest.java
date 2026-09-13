package com.social.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    private static final String ISSUER = "social-auth-service";
    private static final Duration EXPIRATION = Duration.ofMinutes(60);

    @Test
    void acceptsASecretOfExactlyTheMinimumLength() {
        assertThatCode(() -> new JwtProperties("a".repeat(32), ISSUER, EXPIRATION))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsASecretShorterThanTheMinimum() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(31), ISSUER, EXPIRATION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAMissingSecret() {
        assertThatThrownBy(() -> new JwtProperties(null, ISSUER, EXPIRATION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // El valor de fallar al arrancar esta en que el mensaje diga como arreglarlo.
    @Test
    void explainsHowToGenerateTheSecret() {
        assertThatThrownBy(() -> new JwtProperties("corto", ISSUER, EXPIRATION))
                .hasMessageContaining("openssl rand");
    }

    @Test
    void rejectsABlankIssuer() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(32), "   ", EXPIRATION))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAnExpirationThatIsNotPositive() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(32), ISSUER, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtProperties("a".repeat(32), ISSUER, Duration.ofMinutes(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
