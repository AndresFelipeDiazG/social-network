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

    // El defecto es OTHERS: el muro muestra las publicaciones de los demas usuarios.
    // ALL y MINE quedan disponibles como filtro explicito.
    public PostPage list(FeedScope scope, UUID viewerId, PageRequest pageRequest) {
        return posts.findBy(scope == null ? FeedScope.OTHERS : scope, viewerId, pageRequest);
    }
}
