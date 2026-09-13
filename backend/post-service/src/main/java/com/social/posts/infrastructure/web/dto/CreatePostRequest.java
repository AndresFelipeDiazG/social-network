package com.social.posts.infrastructure.web.dto;

import com.social.posts.domain.model.PostMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreatePostRequest(

        @Schema(example = "Buenos dias a todos")
        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = PostMessage.MAX_LENGTH,
                message = "El mensaje no puede superar los " + PostMessage.MAX_LENGTH + " caracteres")
        String message,

        @Schema(description = "Opcional. Si se omite se usa el instante de guardado.",
                example = "2026-09-13T10:00:00Z")
        Instant publishedAt,

        @Schema(description = "Opcional. Identificador devuelto por POST /api/posts/images.")
        UUID imageId) {
}
