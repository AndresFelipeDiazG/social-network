package com.social.posts.infrastructure.web.dto;

import com.social.posts.domain.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String message,
        Instant publishedAt,
        AuthorResponse author,

        @Schema(description = "Cierto si la publicacion es de quien consulta.")
        boolean mine) {

    public static PostResponse from(Post post, UUID viewerId) {
        return new PostResponse(
                post.id(),
                post.message().value(),
                post.publishedAt(),
                new AuthorResponse(
                        post.author().id(),
                        post.author().username(),
                        post.author().displayName()),
                post.belongsTo(viewerId));
    }

    public record AuthorResponse(UUID id, String username, String displayName) {
    }
}
