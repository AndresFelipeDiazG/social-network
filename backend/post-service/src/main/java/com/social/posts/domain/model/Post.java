package com.social.posts.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Post(UUID id, PostMessage message, PostAuthor author, Instant publishedAt) {

    public Post {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la publicacion es obligatorio");
        }
        if (message == null) {
            throw new IllegalArgumentException("El mensaje es obligatorio");
        }
        if (author == null) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }
        if (publishedAt == null) {
            throw new IllegalArgumentException("La fecha de publicacion es obligatoria");
        }
    }

    public boolean belongsTo(UUID userId) {
        return author.id().equals(userId);
    }
}
