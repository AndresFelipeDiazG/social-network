package com.social.posts.domain.usecase;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.PostPage;
import com.social.posts.domain.port.PostRepository;
import java.util.UUID;

public class ListPosts {

    private final PostRepository posts;

    public ListPosts(PostRepository posts) {
        this.posts = posts;
    }

    // El defecto es ALL y no OTHERS: ocultar una publicacion propia justo despues
    // de crearla se percibe como un fallo. El filtro OTHERS queda disponible.
    public PostPage list(FeedScope scope, UUID viewerId, PageRequest pageRequest) {
        return posts.findBy(scope == null ? FeedScope.ALL : scope, viewerId, pageRequest);
    }
}
