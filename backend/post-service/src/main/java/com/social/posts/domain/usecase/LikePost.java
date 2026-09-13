package com.social.posts.domain.usecase;

import com.social.posts.domain.port.PostLikes;
import java.util.UUID;

public class LikePost {

    private final PostLikes likes;

    public LikePost(PostLikes likes) {
        this.likes = likes;
    }

    /**
     * Idempotente: el resultado de marcar es «existe el me gusta», no «uno mas».
     * Un doble clic o un reintento del cliente no pueden inflar el recuento.
     */
    public void like(UUID postId, UUID userId) {
        require(postId, userId);
        likes.add(postId, userId);
    }

    static void require(UUID postId, UUID userId) {
        if (postId == null) {
            throw new IllegalArgumentException("La publicacion es obligatoria");
        }
        if (userId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
    }
}
