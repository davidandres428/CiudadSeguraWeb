package com.ciudadsegura.exception;

import com.ciudadsegura.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de errores: unifica el formato de respuesta de error
 * (timestamp, status, mensaje) para todos los módulos del sistema.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredenciales(CredencialesInvalidasException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarEmailDuplicado(EmailDuplicadoException ex) {
        return construirError(HttpStatus.CONFLICT, "Correo ya registrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" y "));
        return construirError(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construirError(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridad(org.springframework.dao.DataIntegrityViolationException ex) {
        return construirError(HttpStatus.CONFLICT, "Operación no permitida",
                "El registro tiene datos asociados (por ejemplo reportes) o viola una restricción de la base de datos");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(org.springframework.security.access.AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado: " + ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), mensaje));
    }

    private ResponseEntity<Map<String, String>> construirError(HttpStatus status, String error, String detalles) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("detalles", detalles);
        return ResponseEntity.status(status).body(body);
    }
}
