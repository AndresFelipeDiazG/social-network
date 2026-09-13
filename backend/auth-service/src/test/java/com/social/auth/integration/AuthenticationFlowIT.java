package com.social.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.social.auth.domain.port.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Es la unica prueba que demuestra que los hashes del seed son validos: con el
 * hasher mockeado, un hash mal copiado en el SQL pasaria desapercibido.
 */
class AuthenticationFlowIT extends PostgresContainerSupport {

    private static final List<String> SEEDED_USERNAMES =
            List.of("acorrea", "jmendoza", "lvargas", "dcastillo");
    private static final String SEEDED_PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @Test
    void appliesTheMigrationsAndSeedsTheFourUsers() {
        assertThat(SEEDED_USERNAMES)
                .allSatisfy(username -> assertThat(users.findByUsername(username))
                        .as("el usuario %s deberia existir tras las migraciones", username)
                        .isPresent());
    }

    @Test
    void logsInWithSeededCredentialsAndTheTokenOpensProtectedEndpoints() throws Exception {
        String token = login("acorrea", SEEDED_PASSWORD);

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("acorrea"))
                .andExpect(jsonPath("$.displayName").value("Ana Correa"));
    }

    @Test
    void everySeededUserCanLogIn() throws Exception {
        for (String username : SEEDED_USERNAMES) {
            assertThat(login(username, SEEDED_PASSWORD))
                    .as("el usuario %s deberia poder autenticarse", username)
                    .isNotBlank();
        }
    }

    @Test
    void acceptsTheUsernameRegardlessOfCase() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ACorrea", SEEDED_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void issuesTheSameTokenThroughTheBasicAuthorizationVariant() throws Exception {
        String credentials = "acorrea:" + SEEDED_PASSWORD;
        String header = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/auth/login").header(HttpHeaders.AUTHORIZATION, header))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("acorrea"));
    }

    @Test
    void rejectsAWrongPasswordForAnExistingUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("acorrea", "contrasena-incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsARequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATamperedToken() throws Exception {
        String tampered = login("acorrea", SEEDED_PASSWORD) + "x";

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() throws Exception {
        String foreignToken = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEifQ"
                + ".firma-que-no-corresponde-a-nuestro-secreto";

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(body, "$.accessToken");
    }

    private static String loginBody(String username, String password) {
        return "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
    }
}
