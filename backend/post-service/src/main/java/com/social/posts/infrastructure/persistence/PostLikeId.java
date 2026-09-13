package com.social.posts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Clave compuesta: un usuario no puede marcar dos veces la misma publicacion. */
@Embeddable
public class PostLikeId implements Serializable {

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected PostLikeId() {
        // Requerido por JPA.
    }

    PostLikeId(UUID postId, UUID userId) {
        this.postId = postId;
        this.userId = userId;
    }

    UUID getPostId() {
        return postId;
    }

    UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PostLikeId that
                && Objects.equals(postId, that.postId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId);
    }
}
