package com.social.auth.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("No existe ningun usuario con identificador " + id);
    }
}
