package com.social.auth.domain.exception;

/**
 * Se lanza igual si el usuario no existe que si la contrasena no coincide.
 * Distinguir los dos casos permitiria enumerar usuarios validos probando
 * nombres y observando cual de los dos errores responde el servicio.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Usuario o contrasena incorrectos");
    }
}
