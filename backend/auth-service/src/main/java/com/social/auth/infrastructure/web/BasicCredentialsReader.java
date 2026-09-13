package com.social.auth.infrastructure.web;

import com.social.auth.domain.exception.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class BasicCredentialsReader {

    private static final String PREFIX = "Basic ";

    private BasicCredentialsReader() {
    }

    // Un formato invalido devuelve el mismo error que una contrasena incorrecta.
    static Credentials read(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(PREFIX)) {
            throw new InvalidCredentialsException();
        }

        String decoded;
        try {
            byte[] raw = Base64.getDecoder().decode(authorizationHeader.substring(PREFIX.length()).trim());
            decoded = new String(raw, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException malformedBase64) {
            throw new InvalidCredentialsException();
        }

        // Se corta en el primer ':' porque la contrasena puede contener otros.
        int separator = decoded.indexOf(':');
        if (separator < 0) {
            throw new InvalidCredentialsException();
        }

        return new Credentials(decoded.substring(0, separator), decoded.substring(separator + 1));
    }

    record Credentials(String username, String password) {
    }
}
