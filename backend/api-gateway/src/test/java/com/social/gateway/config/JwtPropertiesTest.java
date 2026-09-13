package com.social.gateway.config;

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
    void pointsAtAuthServiceWhenTheSecretIsWrong() {
        assertThatThrownBy(() -> new JwtProperties("corto", ISSUER))
                .hasMessageContaining("auth-service");
    }

    @Test
    void rejectsABlankIssuer() {
        assertThatThrownBy(() -> new JwtProperties("a".repeat(32), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
