package com.social.posts.domain.exception;

import java.util.UUID;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(UUID id) {
        super("No existe la imagen " + id);
    }
}
