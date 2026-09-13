package com.social.auth.infrastructure.web.error;

import com.social.auth.domain.exception.InvalidCredentialsException;
import com.social.auth.domain.exception.UserNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Extiende ResponseEntityExceptionHandler para heredar el tratamiento de las
 * excepciones de Spring MVC: un JSON mal formado sigue devolviendo 400 y un
 * metodo no soportado 405. Sin esa herencia, el manejador generico de Exception
 * los convertiria todos en 500.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail onInvalidCredentials(InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail onUserNotFound(UserNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Usuario no encontrado", exception.getMessage());
    }

    // Registra la causa completa y devuelve un mensaje generico: propagar el
    // detalle de una excepcion no prevista expone clases, consultas y rutas.
    @ExceptionHandler(Exception.class)
    ProblemDetail onUnexpectedFailure(Exception exception) {
        log.error("Error no controlado atendiendo la peticion", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Se ha producido un error inesperado");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Peticion invalida",
                "Alguno de los campos enviados no es valido");
        body.setProperty("errors", fieldErrorsOf(exception));

        return handleExceptionInternal(exception, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    private static Map<String, String> fieldErrorsOf(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            String message = error.getDefaultMessage() == null ? "Valor invalido" : error.getDefaultMessage();
            errors.putIfAbsent(error.getField(), message);
        }
        return errors;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
