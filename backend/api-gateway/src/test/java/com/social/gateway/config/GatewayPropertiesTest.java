package com.social.gateway.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GatewayPropertiesTest {

    private static final String AUTH = "http://auth-service:8081";
    private static final String POSTS = "http://post-service:8082";

    @Test
    void acceptsBothServiceUrls() {
        assertThatCode(() -> new GatewayProperties(AUTH, POSTS, null)).doesNotThrowAnyException();
    }

    @Test
    void rejectsAMissingAuthServiceUrl() {
        assertThatThrownBy(() -> new GatewayProperties(null, POSTS, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auth-service-url");
    }

    @Test
    void rejectsAMissingPostServiceUrl() {
        assertThatThrownBy(() -> new GatewayProperties(AUTH, "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("post-service-url");
    }
}
