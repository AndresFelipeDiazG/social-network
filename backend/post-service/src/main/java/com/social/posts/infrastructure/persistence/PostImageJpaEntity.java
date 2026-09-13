package com.social.posts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "post_images")
@Getter
public class PostImageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    // Sin @Lob: Hibernate mapearia entonces a un objeto grande (oid) de Postgres,
    // y la columna es bytea. Un byte[] a secas se guarda como bytea.
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] bytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostImageJpaEntity() {
        // Requerido por JPA.
    }

    PostImageJpaEntity(UUID id, String contentType, byte[] bytes, Instant createdAt) {
        this.id = id;
        this.contentType = contentType;
        this.bytes = bytes;
        this.sizeBytes = bytes.length;
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PostImageJpaEntity that && id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
