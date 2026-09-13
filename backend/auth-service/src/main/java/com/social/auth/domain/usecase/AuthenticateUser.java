package com.social.auth.domain.usecase;

import com.social.auth.domain.exception.InvalidCredentialsException;
import com.social.auth.domain.model.AuthenticationResult;
import com.social.auth.domain.model.User;
import com.social.auth.domain.port.PasswordHasher;
import com.social.auth.domain.port.TokenIssuer;
import com.social.auth.domain.port.UserRepository;
import java.util.Locale;

public class AuthenticateUser {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthenticateUser(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthenticationResult authenticate(String username, String rawPassword) {
        User user = users.findByUsername(normalize(username))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthenticationResult(tokenIssuer.issue(user), user);
    }

    // La tabla users tiene una restriccion CHECK que garantiza las minusculas.
    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
