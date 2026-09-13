package com.social.posts.infrastructure.web.dto;

import com.social.posts.domain.model.LikeSummary;
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
        boolean mine,

        @Schema(description = "Cuantos me gusta acumula la publicacion.", example = "3")
        long likes,

        @Schema(description = "Cierto si quien consulta le ha dado me gusta.")
        boolean likedByMe,

        @Schema(description = "Ruta de la imagen adjunta, o null si no tiene.")
        String imageUrl) {

    private static final String IMAGE_PATH = "/api/posts/images/";

    public static PostResponse from(Post post, UUID viewerId, LikeSummary likes) {
        return new PostResponse(
                post.id(),
                post.message().value(),
                post.publishedAt(),
                new AuthorResponse(
                        post.author().id(),
                        post.author().username(),
                        post.author().displayName()),
                post.belongsTo(viewerId),
                likes.total(),
                likes.likedByViewer(),
                post.hasImage() ? IMAGE_PATH + post.imageId() : null);
    }

    public record AuthorResponse(UUID id, String username, String displayName) {
    }
}
