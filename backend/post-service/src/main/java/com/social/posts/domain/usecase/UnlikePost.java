package com.social.posts.domain.usecase;

import com.social.posts.domain.port.PostLikes;
import java.util.UUID;

public class UnlikePost {

    private final PostLikes likes;

    public UnlikePost(PostLikes likes) {
        this.likes = likes;
    }

    /** Quitar un me gusta que no estaba puesto no es un error, es el mismo estado. */
    public void unlike(UUID postId, UUID userId) {
        LikePost.require(postId, userId);
        likes.remove(postId, userId);
    }
}
