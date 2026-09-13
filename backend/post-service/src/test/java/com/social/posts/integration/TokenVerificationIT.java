package com.social.posts.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Comprueba que un token emitido con el secreto compartido lo acepta este servicio
 * sin hablar con auth-service: la verificacion de la firma es local y matematica.
 *
 * <p>Los tokens se fabrican aqui con el mismo secreto y el mismo formato de claims
 * que usa auth-service, de modo que la prueba tambien fija ese contrato.
 */
@AutoConfigureMockMvc
class TokenVerificationIT extends PostgresContainerSupport {

    private static final String SHARED_SECRET = "secreto-exclusivo-de-pruebas-con-mas-de-32-bytes";
    private static final String EXPECTED_ISSUER = "social-auth-service-test";

    // Endpoint que existe y no esta en la lista de rutas publicas, asi que sirve
    // para distinguir "rechazado por seguridad" de "no hay manejador".
    private static final String PROTECTED_PATH = "/actuator/info";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsARequestWithoutAnyToken() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsATokenSignedWithTheSharedSecret() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, bearerOf(validToken())))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() throws Exception {
        String foreign = tokenSignedWith(
                "otro-secreto-completamente-distinto-de-32-bytes", EXPECTED_ISSUER, oneHourFromNow());

        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, bearerOf(foreign)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATamperedToken() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, bearerOf(validToken() + "x")))
                .andExpect(status().isUnauthorized());
    }

    // Diez minutos atras y no uno: el validador aplica una tolerancia de reloj de
    // sesenta segundos, asi que un token recien caducado todavia se aceptaria.
    @Test
    void rejectsAnExpiredToken() throws Exception {
        String expired = tokenSignedWith(
                SHARED_SECRET, EXPECTED_ISSUER, Instant.now().minusSeconds(600));

        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, bearerOf(expired)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATokenFromAnotherIssuer() throws Exception {
        String foreignIssuer = tokenSignedWith(SHARED_SECRET, "otro-emisor", oneHourFromNow());

        mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, bearerOf(foreignIssuer)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leavesHealthAndDocumentationOpen() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private static String bearerOf(String token) {
        return "Bearer " + token;
    }

    private static String validToken() {
        return tokenSignedWith(SHARED_SECRET, EXPECTED_ISSUER, oneHourFromNow());
    }

    private static Instant oneHourFromNow() {
        return Instant.now().plusSeconds(3600);
    }

    private static String tokenSignedWith(String secret, String issuer, Instant expiresAt) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now().minusSeconds(3600))
                .expiresAt(expiresAt)
                .claim("username", "acorrea")
                .claim("displayName", "Ana Correa")
                .build();

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
