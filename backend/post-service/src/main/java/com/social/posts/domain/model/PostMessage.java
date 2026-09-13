package com.social.posts.domain.model;

import com.social.posts.domain.exception.InvalidPostMessageException;

public record PostMessage(String value) {

    public static final int MAX_LENGTH = 280;

    public PostMessage(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidPostMessageException("El mensaje no puede estar vacio");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidPostMessageException(
                    "El mensaje no puede superar los " + MAX_LENGTH + " caracteres");
        }
        this.value = trimmed;
    }
}
