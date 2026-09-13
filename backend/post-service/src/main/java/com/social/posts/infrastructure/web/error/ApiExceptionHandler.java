package com.social.posts.infrastructure.web.error;

import com.social.posts.domain.exception.ImageNotFoundException;
import com.social.posts.domain.exception.InvalidImageException;
import com.social.posts.domain.exception.InvalidPostMessageException;
import com.social.posts.domain.exception.PublicationDateInFutureException;
import com.social.posts.infrastructure.security.InvalidTokenClaimsException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Extiende ResponseEntityExceptionHandler para heredar el tratamiento de las
 * excepciones de Spring MVC: un JSON mal formado sigue devolviendo 400, un
 * parametro de tipo incorrecto tambien, y un archivo que supera el limite de
 * subida, 413. Sin esa herencia, el manejador generico de
 * Exception los convertiria todos en 500.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidPostMessageException.class)
    ProblemDetail onInvalidMessage(InvalidPostMessageException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Publicacion invalida", exception.getMessage());
    }

    @ExceptionHandler(InvalidImageException.class)
    ProblemDetail onInvalidImage(InvalidImageException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Imagen invalida", exception.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    ProblemDetail onImageNotFound(ImageNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Imagen no encontrada", exception.getMessage());
    }

    /**
     * Salta cuando la publicacion apunta a una imagen que no existe: la clave
     * ajena lo impide en la base de datos. Es un 400 y no un 500 porque el dato
     * que esta mal lo ha enviado el cliente.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail onBrokenReference(DataIntegrityViolationException exception) {
        log.warn("Referencia invalida al guardar: {}", exception.getMostSpecificCause().getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Referencia invalida",
                "La imagen indicada no existe");
    }

    @ExceptionHandler(PublicationDateInFutureException.class)
    ProblemDetail onFutureDate(PublicationDateInFutureException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Fecha invalida", exception.getMessage());
    }

    /**
     * El token esta firmado por nosotros pero le falta un claim del contrato. Se
     * responde 401 y no 500: para el cliente el token es inservible, y el detalle
     * del claim ausente no le ayuda a arreglarlo.
     */
    @ExceptionHandler(InvalidTokenClaimsException.class)
    ProblemDetail onInvalidTokenClaims(InvalidTokenClaimsException exception) {
        log.warn("Token con claims incompletos: {}", exception.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "Token invalido", "El token no es utilizable");
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
