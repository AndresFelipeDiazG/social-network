package com.social.posts.infrastructure.security;

import com.social.posts.domain.model.PostAuthor;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Los nombres de los claims son el contrato con auth-service. Al estar en un solo
 * sitio, un cambio en ese contrato se toca aqui y no repartido por los
 * controladores.
 */
public final class JwtAuthorReader {

    private static final String USERNAME = "username";
    private static final String DISPLAY_NAME = "displayName";

    private JwtAuthorReader() {
    }

    public static PostAuthor read(Jwt token) {
        return new PostAuthor(
                identifierOf(token),
                requireText(token, USERNAME),
                requireText(token, DISPLAY_NAME));
    }

    private static UUID identifierOf(Jwt token) {
        String subject = token.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new InvalidTokenClaimsException("sub");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException notAUuid) {
            throw new InvalidTokenClaimsException("sub");
        }
    }

    private static String requireText(Jwt token, String claim) {
        String value = token.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw new InvalidTokenClaimsException(claim);
        }
        return value;
    }
}
