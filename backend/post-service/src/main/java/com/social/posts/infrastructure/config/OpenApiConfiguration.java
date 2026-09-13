package com.social.posts.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    OpenAPI postServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Post Service")
                        .version("1.0.0")
                        .description("""
                                Muro de publicaciones y creacion de mensajes.

                                Todos los endpoints exigen un token emitido por
                                auth-service. Para probarlos: hacer login en
                                auth-service, copiar el accessToken y pegarlo en el
                                boton Authorize de arriba.
                                """))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
