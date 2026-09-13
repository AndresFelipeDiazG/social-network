package com.social.posts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Entity
@Table(name = "post_likes")
@Getter
public class PostLikeJpaEntity {

    @EmbeddedId
    private PostLikeId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostLikeJpaEntity() {
        // Requerido por JPA.
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PostLikeJpaEntity that && id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
