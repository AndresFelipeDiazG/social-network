package com.social.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Schema(example = "acorrea")
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
        String username,

        @Schema(example = "Password123!")
        @NotBlank(message = "La contrasena es obligatoria")
        String password) {
}
