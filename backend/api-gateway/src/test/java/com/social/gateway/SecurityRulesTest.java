package com.social.gateway;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Las URL de los servicios apuntan en pruebas a dos puertos donde no escucha nadie.
 * Eso hace que una peticion que atraviesa la seguridad falle al conectar, y el
 * mensaje de esa excepcion contiene la URL de destino: comprobarla verifica a la vez
 * que la seguridad dejo pasar la peticion y que la ruta apunta al servicio correcto,
 * sin necesidad de un servidor simulado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    private static final String SHARED_SECRET = "secreto-exclusivo-de-pruebas-con-mas-de-32-bytes";
    private static final String EXPECTED_ISSUER = "social-auth-service-test";
    private static final String AUTH_SERVICE = "http://localhost:18081";
    private static final String POST_SERVICE = "http://localhost:18082";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsPostsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/posts")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATamperedToken() throws Exception {
        mockMvc.perform(get("/api/posts").header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken() + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() throws Exception {
        String foreign = tokenSignedWith("otro-secreto-completamente-distinto-de-32-bytes", EXPECTED_ISSUER);

        mockMvc.perform(get("/api/posts").header(HttpHeaders.AUTHORIZATION, "Bearer " + foreign))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void routesTheLoginToAuthServiceWithoutRequiringAToken() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api/auth/login")))
                .hasMessageContaining(AUTH_SERVICE + "/api/auth/login");
    }

    @Test
    void routesPostsToPostServiceOnceTheTokenIsValid() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api/posts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken())))
                .hasMessageContaining(POST_SERVICE + "/api/posts");
    }

    // El prefijo /api-docs se reescribe a /v3/api-docs de cada servicio, porque el
    // navegador no puede resolver los nombres de la red de Docker.
    @Test
    void rewritesTheDocumentationPathForEachService() {
        assertThatThrownBy(() -> mockMvc.perform(get("/api-docs/auth")))
                .hasMessageContaining(AUTH_SERVICE + "/v3/api-docs");

        assertThatThrownBy(() -> mockMvc.perform(get("/api-docs/posts")))
                .hasMessageContaining(POST_SERVICE + "/v3/api-docs");
    }

    @Test
    void leavesHealthAndDocumentationOpen() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        // springdoc redirige a /swagger-ui/index.html
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    /**
     * El gateway no es un proxy general: lo que no esta declarado no pasa. Sin token
     * responde 401 porque no hay identidad que evaluar; con token valido responde 403,
     * que es la negacion explicita.
     */
    @Test
    void refusesAnyPathThatIsNotDeclared() throws Exception {
        mockMvc.perform(get("/interno/metricas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/interno/metricas").header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken()))
                .andExpect(status().isForbidden());
    }

    private static String validToken() {
        return tokenSignedWith(SHARED_SECRET, EXPECTED_ISSUER);
    }

    private static String tokenSignedWith(String secret, String issuer) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("username", "acorrea")
                .claim("displayName", "Ana Correa")
                .build();

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
