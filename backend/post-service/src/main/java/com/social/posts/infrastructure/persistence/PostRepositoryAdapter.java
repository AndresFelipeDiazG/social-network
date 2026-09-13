package com.social.posts.infrastructure.persistence;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostPage;
import com.social.posts.domain.port.PostRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostRepositoryAdapter implements PostRepository {

    // El desempate por id hace la paginacion estable: sin el, dos publicaciones
    // con la misma fecha pueden salir en dos paginas distintas o en ninguna.
    private static final Sort NEWEST_FIRST =
            Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.asc("id"));

    private final SpringDataPostRepository repository;

    public PostRepositoryAdapter(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Post save(Post post) {
        return PostPersistenceMapper.toDomain(repository.save(PostPersistenceMapper.toEntity(post)));
    }

    @Override
    @Transactional(readOnly = true)
    public PostPage findBy(FeedScope scope, UUID viewerId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), NEWEST_FIRST);

        // Switch de expresion sobre el enum: el compilador exige cubrir todos los
        // casos, asi que anadir un scope nuevo rompe el build hasta tratarlo aqui.
        // Un mapa de estrategias no daria esa garantia, fallaria en ejecucion.
        Page<PostJpaEntity> found = switch (scope) {
            case ALL -> repository.findAll(pageable);
            case OTHERS -> repository.findByAuthorIdNot(viewerId, pageable);
            case MINE -> repository.findByAuthorId(viewerId, pageable);
        };

        return new PostPage(
                found.getContent().stream().map(PostPersistenceMapper::toDomain).toList(),
                pageRequest.page(),
                pageRequest.size(),
                found.getTotalElements());
    }
}
