package com.social.posts.infrastructure.persistence;

import com.social.posts.domain.model.StoredImage;
import com.social.posts.domain.port.PostImages;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Los bytes viven en la propia base de datos. Un volumen seria lo correcto en
 * produccion, pero aqui la entrega tiene que levantar con un solo comando y
 * quedar limpia con «docker compose down -v»: un directorio montado añade rutas,
 * permisos y copias de seguridad aparte para dos megabytes por imagen.
 */
@Repository
public class PostImagesAdapter implements PostImages {

    private final SpringDataPostImageRepository repository;
    private final Clock clock;

    public PostImagesAdapter(SpringDataPostImageRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UUID save(String contentType, byte[] content) {
        PostImageJpaEntity saved = repository.save(new PostImageJpaEntity(
                UUID.randomUUID(), contentType, content, Instant.now(clock)));

        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredImage> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> new StoredImage(entity.getId(), entity.getContentType(), entity.getBytes()));
    }
}
