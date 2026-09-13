package com.social.auth.domain.port;

import com.social.auth.domain.model.PasswordHash;

// Sin metodo para generar hashes: el servicio no registra usuarios, los siembra Flyway.
public interface PasswordHasher {

    boolean matches(String rawPassword, PasswordHash hash);
}
