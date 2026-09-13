package com.social.posts.domain.model;

/** Cuantos me gusta tiene una publicacion y si quien mira es uno de ellos. */
public record LikeSummary(long total, boolean likedByViewer) {

    public static final LikeSummary NONE = new LikeSummary(0, false);

    public LikeSummary {
        if (total < 0) {
            throw new IllegalArgumentException("El recuento de me gusta no puede ser negativo");
        }
    }
}
