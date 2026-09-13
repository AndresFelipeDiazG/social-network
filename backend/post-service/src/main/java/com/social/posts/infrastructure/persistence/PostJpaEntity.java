package com.social.posts.infrastructure.persistence;

import com.social.posts.domain.model.PostMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "posts")
@Getter
public class PostJpaEntity {

    @Id
    private UUID id;

    // La longitud sale de la constante del dominio, de modo que el limite de
    // negocio y el de la columna no pueden divergir.
    @Column(nullable = false, length = PostMessage.MAX_LENGTH)
    private String message;

    // El autor va desnormalizado y no hay clave ajena: la tabla users vive en la
    // base de datos de auth-service.
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_username", nullable = false, length = 50)
    private String authorUsername;

    @Column(name = "author_display_name", nullable = false, length = 100)
    private String authorDisplayName;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    protected PostJpaEntity() {
        // Requerido por JPA.
    }

    PostJpaEntity(UUID id, String message, UUID authorId, String authorUsername,
            String authorDisplayName, Instant publishedAt) {
        this.id = id;
        this.message = message;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.publishedAt = publishedAt;
    }

    // Solo por identificador: con campos mutables, dos referencias a la misma fila
    // dejan de ser iguales al cambiar una, y eso rompe los Set que las contengan.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PostJpaEntity that && id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
