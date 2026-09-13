package com.social.auth.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.social.auth.domain.exception.InvalidCredentialsException;
import com.social.auth.domain.model.AccessToken;
import com.social.auth.domain.model.AuthenticationResult;
import com.social.auth.domain.model.PasswordHash;
import com.social.auth.domain.model.User;
import com.social.auth.domain.usecase.AuthenticateUser;
import com.social.auth.domain.usecase.FindUserById;
import com.social.auth.infrastructure.security.SecurityConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de slice: solo la capa web. Los casos de uso se sustituyen por mocks
 * ({@code @MockitoBean}, porque {@code @MockBean} se elimino en Spring Boot 4),
 * asi que no hay base de datos ni Docker en juego.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String STORED_HASH =
            "$2a$10$NEpssAucp9CBbxqg/hz0xelk2gFYH8vdZWrqOPj68SvEZktN3krsC";

    private static final String VALID_BODY = """
            {"username": "acorrea", "password": "Password123!"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUser authenticateUser;

    @MockitoBean
    private FindUserById findUserById;

    private final User user = new User(USER_ID, "acorrea", "Ana Correa", new PasswordHash(STORED_HASH));

    @Test
    void returnsTokenAndUserWhenLoginSucceeds() throws Exception {
        when(authenticateUser.authenticate("acorrea", "Password123!")).thenReturn(successfulResult());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-firmado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.username").value("acorrea"))
                .andExpect(jsonPath("$.user.displayName").value("Ana Correa"));
    }

    @Test
    void returns401WhenCredentialsAreInvalid() throws Exception {
        when(authenticateUser.authenticate(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciales invalidas"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void returns400AndNamesTheOffendingFieldWhenUsernameIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "Password123!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void returns400WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "acorrea", "password": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void returns400WhenTheBodyIsNotValidJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsCredentialsFromABasicAuthorizationHeader() throws Exception {
        when(authenticateUser.authenticate("acorrea", "Password123!")).thenReturn(successfulResult());

        mockMvc.perform(get("/api/auth/login")
                        .header(HttpHeaders.AUTHORIZATION, basicHeader("acorrea", "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-firmado"));
    }

    @Test
    void returns401WhenTheBasicHeaderIsAbsent() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns401WhenTheBasicHeaderIsNotValidBase64() throws Exception {
        mockMvc.perform(get("/api/auth/login").header(HttpHeaders.AUTHORIZATION, "Basic no-es-base64!!"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTheProtectedEndpointWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * La respuesta se construye con un record que no tiene campo para el hash, de
     * modo que no puede filtrarlo. Esta prueba fija ese comportamiento para que
     * nadie lo rompa sustituyendo el DTO por la entidad.
     */
    @Test
    void neverSerializesThePasswordHash() throws Exception {
        when(authenticateUser.authenticate("acorrea", "Password123!")).thenReturn(successfulResult());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString(STORED_HASH))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("passwordHash"))))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    private AuthenticationResult successfulResult() {
        Instant issuedAt = Instant.parse("2026-01-01T10:00:00Z");
        return new AuthenticationResult(
                new AccessToken("token-firmado", issuedAt, issuedAt.plusSeconds(3600)),
                user);
    }

    private static String basicHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
