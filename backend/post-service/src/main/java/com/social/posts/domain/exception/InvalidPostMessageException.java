package com.social.posts.domain.exception;

public class InvalidPostMessageException extends RuntimeException {

    public InvalidPostMessageException(String message) {
        super(message);
    }
}
