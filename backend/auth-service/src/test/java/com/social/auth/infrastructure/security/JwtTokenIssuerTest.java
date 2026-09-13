package com.social.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.social.auth.domain.model.AccessToken;
import com.social.auth.domain.model.PasswordHash;
import com.social.auth.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenIssuerTest {

    private static final String SECRET = "secreto-exclusivo-de-pruebas-con-mas-de-32-bytes";
    private static final String ISSUER = "social-auth-service-test";
    private static final Duration EXPIRATION = Duration.ofMinutes(60);
    private static final String STORED_HASH =
            "$2a$10$NEpssAucp9CBbxqg/hz0xelk2gFYH8vdZWrqOPj68SvEZktN3krsC";

    // No es una fecha literal porque el decoder valida la expiracion contra el
    // reloj del sistema: un token emitido en el pasado se rechazaria por caducado
    // antes de poder inspeccionar sus claims.
    private static final Instant ISSUED_AT = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    private final SecretKey key =
            new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtTokenIssuer issuer = new JwtTokenIssuer(
            new NimbusJwtEncoder(new ImmutableSecret<>(key)),
            new JwtProperties(SECRET, ISSUER, EXPIRATION),
            Clock.fixed(ISSUED_AT, ZoneOffset.UTC));

    private final User user = new User(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "acorrea",
            "Ana Correa",
            new PasswordHash(STORED_HASH));

    @Test
    void usesTheUserIdentifierAsSubject() {
        Jwt decoded = decode(issuer.issue(user));

        assertThat(decoded.getSubject()).isEqualTo(user.id().toString());
    }

    @Test
    void includesUsernameAndDisplayNameAsClaims() {
        Jwt decoded = decode(issuer.issue(user));

        assertThat(decoded.getClaimAsString("username")).isEqualTo("acorrea");
        assertThat(decoded.getClaimAsString("displayName")).isEqualTo("Ana Correa");
    }

    @Test
    void expiresAfterTheConfiguredDuration() {
        AccessToken token = issuer.issue(user);

        assertThat(token.issuedAt()).isEqualTo(ISSUED_AT);
        assertThat(token.expiresAt()).isEqualTo(ISSUED_AT.plus(EXPIRATION));
        assertThat(token.secondsUntilExpiry()).isEqualTo(EXPIRATION.toSeconds());
    }

    // Como cadena y no con getIssuer(), que devuelve URL: RFC 7519 define iss como
    // StringOrURI y aqui es un identificador simple, no una URL.
    @Test
    void signsWithTheConfiguredIssuer() {
        Jwt decoded = decode(issuer.issue(user));

        assertThat(decoded.getClaimAsString("iss")).isEqualTo(ISSUER);
    }

    @Test
    void producesATokenThatTheMatchingDecoderAccepts() {
        AccessToken token = issuer.issue(user);

        assertThat(decode(token)).isNotNull();
    }

    /**
     * El payload de un JWT va firmado pero no cifrado: se lee en base64 sin
     * ninguna clave. Un hash ahi dentro seria un hash publico.
     */
    @Test
    void neverPutsThePasswordHashInTheToken() {
        AccessToken token = issuer.issue(user);

        assertThat(decode(token).getClaims().values())
                .noneMatch(claim -> String.valueOf(claim).contains(STORED_HASH));
        assertThat(token.value()).doesNotContain(STORED_HASH);
    }

    private Jwt decode(AccessToken token) {
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        return decoder.decode(token.value());
    }
}
