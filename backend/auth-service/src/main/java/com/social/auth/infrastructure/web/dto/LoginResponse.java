package com.social.auth.infrastructure.web.dto;

import com.social.auth.domain.model.AuthenticationResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(

        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Segundos que quedan hasta que el token expire", example = "3600")
        long expiresIn,

        AuthenticatedUserResponse user) {

    private static final String BEARER = "Bearer";

    public static LoginResponse from(AuthenticationResult result) {
        return new LoginResponse(
                result.token().value(),
                BEARER,
                result.token().secondsUntilExpiry(),
                AuthenticatedUserResponse.from(result.user()));
    }
}
