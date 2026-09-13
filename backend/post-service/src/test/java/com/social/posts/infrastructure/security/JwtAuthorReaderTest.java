package com.social.posts.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.social.posts.domain.model.PostAuthor;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthorReaderTest {

    private static final UUID SUBJECT = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void buildsTheAuthorFromTheTokenClaims() {
        PostAuthor author = JwtAuthorReader.read(tokenWith(Map.of(
                "sub", SUBJECT.toString(),
                "username", "acorrea",
                "displayName", "Ana Correa")));

        assertThat(author.id()).isEqualTo(SUBJECT);
        assertThat(author.username()).isEqualTo("acorrea");
        assertThat(author.displayName()).isEqualTo("Ana Correa");
    }

    @Test
    void rejectsATokenWithoutUsername() {
        Jwt token = tokenWith(Map.of("sub", SUBJECT.toString(), "displayName", "Ana Correa"));

        assertThatThrownBy(() -> JwtAuthorReader.read(token))
                .isInstanceOf(InvalidTokenClaimsException.class)
                .hasMessageContaining("username");
    }

    @Test
    void rejectsATokenWithoutDisplayName() {
        Jwt token = tokenWith(Map.of("sub", SUBJECT.toString(), "username", "acorrea"));

        assertThatThrownBy(() -> JwtAuthorReader.read(token))
                .isInstanceOf(InvalidTokenClaimsException.class)
                .hasMessageContaining("displayName");
    }

    @Test
    void rejectsABlankClaim() {
        Jwt token = tokenWith(Map.of(
                "sub", SUBJECT.toString(), "username", "   ", "displayName", "Ana Correa"));

        assertThatThrownBy(() -> JwtAuthorReader.read(token))
                .isInstanceOf(InvalidTokenClaimsException.class);
    }

    // Un sub que no sea UUID solo puede venir de un emisor que no es el nuestro, o
    // de un cambio de formato: en los dos casos el token es inservible aqui.
    @Test
    void rejectsASubjectThatIsNotAUuid() {
        Jwt token = tokenWith(Map.of(
                "sub", "no-es-un-uuid", "username", "acorrea", "displayName", "Ana Correa"));

        assertThatThrownBy(() -> JwtAuthorReader.read(token))
                .isInstanceOf(InvalidTokenClaimsException.class)
                .hasMessageContaining("sub");
    }

    private static Jwt tokenWith(Map<String, Object> claims) {
        return Jwt.withTokenValue("no-se-verifica-en-esta-prueba")
                .header("alg", "HS256")
                .claims(existing -> existing.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
