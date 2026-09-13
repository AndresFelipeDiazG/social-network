package com.social.auth.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";

    // El esquema se declara pero no se aplica globalmente: cada operacion protegida
    // lo pide. Aplicarlo a todo documentaria el login como si necesitara un token.
    @Bean
    OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service")
                        .version("1.0.0")
                        .description("""
                                Autenticacion de usuarios y emision de tokens JWT.

                                Para probar los endpoints protegidos: ejecutar un login,
                                copiar el valor de accessToken y pegarlo en el boton
                                Authorize de arriba.
                                """))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
