package com.social.posts.domain.exception;

import java.time.Instant;

public class PublicationDateInFutureException extends RuntimeException {

    public PublicationDateInFutureException(Instant publishedAt) {
        super("La fecha de publicacion no puede estar en el futuro: " + publishedAt);
    }
}
