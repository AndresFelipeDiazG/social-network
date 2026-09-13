package com.social.posts.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, UUID> {

    // El orden no va en el nombre del metodo: lo aporta el Pageable que construye
    // el adaptador, para que las tres consultas compartan un unico criterio.
    Page<PostJpaEntity> findByAuthorIdNot(UUID authorId, Pageable pageable);

    Page<PostJpaEntity> findByAuthorId(UUID authorId, Pageable pageable);
}
