package com.social.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "social.gateway")
public record GatewayProperties(String authServiceUrl, String postServiceUrl, String allowedOrigins) {

    public GatewayProperties {
        requireUrl(authServiceUrl, "social.gateway.auth-service-url");
        requireUrl(postServiceUrl, "social.gateway.post-service-url");
    }

    private static void requireUrl(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(property + " es obligatorio");
        }
    }
}
