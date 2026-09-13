package com.social.gateway.config;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * En Spring Cloud Gateway 5.x, http() no recibe el destino: lo aporta el filtro
 * before(uri(...)). La forma http("http://host") de versiones anteriores ya no
 * existe.
 */
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class RouteConfiguration {

    @Bean
    RouterFunction<ServerResponse> authServiceRoute(GatewayProperties properties) {
        return route("auth-service")
                .route(path("/api/auth/**"), http())
                .before(uri(properties.authServiceUrl()))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> postServiceRoute(GatewayProperties properties) {
        return route("post-service")
                .route(path("/api/posts/**"), http())
                .before(uri(properties.postServiceUrl()))
                .build();
    }

    /**
     * Los documentos de OpenAPI van bajo un prefijo propio y no bajo /api/auth ni
     * /api/posts, para que no compitan con las rutas de la API: dos predicados que
     * se solapan dependerian del orden de los beans, que no es explicito.
     *
     * <p>El navegador no puede resolver los nombres de servicio de la red de Docker,
     * asi que el gateway tiene que servir estos documentos por proxy en lugar de
     * apuntar la interfaz de Swagger directamente a cada servicio.
     */
    @Bean
    RouterFunction<ServerResponse> authApiDocsRoute(GatewayProperties properties) {
        return route("auth-service-api-docs")
                .route(path("/api-docs/auth"), http())
                .before(rewritePath("/api-docs/auth", "/v3/api-docs"))
                .before(uri(properties.authServiceUrl()))
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> postApiDocsRoute(GatewayProperties properties) {
        return route("post-service-api-docs")
                .route(path("/api-docs/posts"), http())
                .before(rewritePath("/api-docs/posts", "/v3/api-docs"))
                .before(uri(properties.postServiceUrl()))
                .build();
    }
}
