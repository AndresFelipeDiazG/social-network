package com.social.posts.infrastructure.persistence;

import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.model.PostMessage;

final class PostPersistenceMapper {

    private PostPersistenceMapper() {
    }

    static PostJpaEntity toEntity(Post post) {
        return new PostJpaEntity(
                post.id(),
                post.message().value(),
                post.author().id(),
                post.author().username(),
                post.author().displayName(),
                post.publishedAt());
    }

    static Post toDomain(PostJpaEntity entity) {
        return new Post(
                entity.getId(),
                new PostMessage(entity.getMessage()),
                new PostAuthor(entity.getAuthorId(), entity.getAuthorUsername(), entity.getAuthorDisplayName()),
                entity.getPublishedAt());
    }
}
