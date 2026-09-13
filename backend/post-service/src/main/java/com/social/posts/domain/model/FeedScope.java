package com.social.posts.domain.model;

public enum FeedScope {

    /** Todas las publicaciones, incluidas las de quien consulta. */
    ALL,

    /** Solo las de los demas usuarios. */
    OTHERS,

    /** Solo las de quien consulta. */
    MINE
}
