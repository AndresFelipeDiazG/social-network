package com.social.auth.domain.model;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El hash de la contrasena no puede estar vacio");
        }
    }

    /**
     * Un hash filtrado a los registros se puede atacar sin limite de intentos,
     * fuera del alcance de cualquier restriccion de frecuencia del servicio.
     */
    @Override
    public String toString() {
        return "PasswordHash[oculto]";
    }
}
