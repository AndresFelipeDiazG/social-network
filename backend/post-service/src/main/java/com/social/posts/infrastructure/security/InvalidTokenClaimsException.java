package com.social.posts.infrastructure.security;

public class InvalidTokenClaimsException extends RuntimeException {

    public InvalidTokenClaimsException(String claim) {
        super("El token no contiene un valor valido para el claim " + claim);
    }
}
