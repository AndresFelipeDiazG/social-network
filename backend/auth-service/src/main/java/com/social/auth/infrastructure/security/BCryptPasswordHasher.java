package com.social.auth.infrastructure.security;

import com.social.auth.domain.model.PasswordHash;
import com.social.auth.domain.port.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash) {
        return rawPassword != null && encoder.matches(rawPassword, hash.value());
    }
}
