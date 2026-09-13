package com.social.auth.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.social.auth.domain.exception.InvalidCredentialsException;
import com.social.auth.domain.model.AccessToken;
import com.social.auth.domain.model.AuthenticationResult;
import com.social.auth.domain.model.PasswordHash;
import com.social.auth.domain.model.User;
import com.social.auth.domain.port.PasswordHasher;
import com.social.auth.domain.port.TokenIssuer;
import com.social.auth.domain.port.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticateUserTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final PasswordHash STORED_HASH = new PasswordHash("$2a$10$hash-almacenado");
    private static final String CORRECT_PASSWORD = "Password123!";

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final TokenIssuer tokenIssuer = mock(TokenIssuer.class);

    private final AuthenticateUser authenticateUser =
            new AuthenticateUser(users, passwordHasher, tokenIssuer);

    private final User storedUser = new User(USER_ID, "acorrea", "Ana Correa", STORED_HASH);

    @Test
    void issuesTokenWhenCredentialsAreValid() {
        AccessToken expectedToken = anyValidToken();
        when(users.findByUsername("acorrea")).thenReturn(Optional.of(storedUser));
        when(passwordHasher.matches(CORRECT_PASSWORD, STORED_HASH)).thenReturn(true);
        when(tokenIssuer.issue(storedUser)).thenReturn(expectedToken);

        AuthenticationResult result = authenticateUser.authenticate("acorrea", CORRECT_PASSWORD);

        assertThat(result.token()).isEqualTo(expectedToken);
        assertThat(result.user()).isEqualTo(storedUser);
    }

    @Test
    void rejectsUnknownUsername() {
        when(users.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticateUser.authenticate("desconocido", CORRECT_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsWrongPassword() {
        when(users.findByUsername("acorrea")).thenReturn(Optional.of(storedUser));
        when(passwordHasher.matches("incorrecta", STORED_HASH)).thenReturn(false);

        assertThatThrownBy(() -> authenticateUser.authenticate("acorrea", "incorrecta"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void reportsTheSameErrorForUnknownUsernameAndWrongPassword() {
        when(users.findByUsername("desconocido")).thenReturn(Optional.empty());
        when(users.findByUsername("acorrea")).thenReturn(Optional.of(storedUser));
        when(passwordHasher.matches("incorrecta", STORED_HASH)).thenReturn(false);

        String unknownUser = messageOf(() -> authenticateUser.authenticate("desconocido", CORRECT_PASSWORD));
        String wrongPassword = messageOf(() -> authenticateUser.authenticate("acorrea", "incorrecta"));

        assertThat(unknownUser).isEqualTo(wrongPassword);
    }

    @Test
    void doesNotIssueAnyTokenWhenThePasswordDoesNotMatch() {
        when(users.findByUsername("acorrea")).thenReturn(Optional.of(storedUser));
        when(passwordHasher.matches(any(), eq(STORED_HASH))).thenReturn(false);

        assertThatThrownBy(() -> authenticateUser.authenticate("acorrea", "incorrecta"))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void normalizesUsernameBeforeLookingItUp() {
        when(users.findByUsername("acorrea")).thenReturn(Optional.of(storedUser));
        when(passwordHasher.matches(CORRECT_PASSWORD, STORED_HASH)).thenReturn(true);
        when(tokenIssuer.issue(storedUser)).thenReturn(anyValidToken());

        authenticateUser.authenticate("  ACorrea  ", CORRECT_PASSWORD);

        verify(users).findByUsername("acorrea");
    }

    @Test
    void treatsNullUsernameAsInvalidInsteadOfFailingWithNullPointer() {
        when(users.findByUsername("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticateUser.authenticate(null, CORRECT_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private static AccessToken anyValidToken() {
        Instant issuedAt = Instant.parse("2026-01-01T10:00:00Z");
        return new AccessToken("token-de-prueba", issuedAt, issuedAt.plusSeconds(3600));
    }

    private static String messageOf(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Se esperaba InvalidCredentialsException");
        } catch (InvalidCredentialsException expected) {
            return expected.getMessage();
        }
    }
}
