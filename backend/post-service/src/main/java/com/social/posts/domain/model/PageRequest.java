package com.social.posts.domain.model;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 20;

    // Sin tope, un cliente puede pedir size=1000000 y forzar al servicio a cargar
    // la tabla entera en memoria.
    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("La pagina no puede ser negativa");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "El tamano de pagina debe estar entre 1 y " + MAX_SIZE);
        }
    }

    public static PageRequest of(Integer page, Integer size) {
        return new PageRequest(
                page == null ? 0 : page,
                size == null ? DEFAULT_SIZE : size);
    }
}
