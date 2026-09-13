package com.social.auth.domain.model;

public record AuthenticationResult(AccessToken token, User user) {
}
