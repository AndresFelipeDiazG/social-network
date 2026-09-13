package com.social.posts.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    private static final String ISSUER = "social-auth-service";

    @Test
    void acceptsASecretOfExactlyTheMinimumLength() {
        assertThatCode(() -> new JwtProperties("a".repeat(32), ISSUER)).doesNotThrowAnyException();
    }

    @Test
    void rejectsASecretShorterThanTheMinimum() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(31), ISSUER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAMissingSecret() {
        assertThatThrownBy(() -> new JwtProperties(null, ISSUER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsABlankIssuer() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(32), "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // El error mas probable no es un secreto corto, es uno que no coincide con el de
    // auth-service. El mensaje tiene que apuntar ahi.
    @Test
    void pointsAtAuthServiceWhenTheSecretIsWrong() {
        assertThatThrownBy(() -> new JwtProperties("corto", ISSUER))
                .hasMessageContaining("auth-service");
    }
}
