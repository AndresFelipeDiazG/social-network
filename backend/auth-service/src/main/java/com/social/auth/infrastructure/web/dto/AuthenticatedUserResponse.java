package com.social.auth.infrastructure.web.dto;

import com.social.auth.domain.model.User;
import java.util.UUID;

public record AuthenticatedUserResponse(UUID id, String username, String displayName) {

    public static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.id(), user.username(), user.displayName());
    }
}
