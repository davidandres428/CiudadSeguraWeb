package com.ciudadsegura.exception;

import com.ciudadsegura.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para los servicios de autenticación.
 *
 * Centraliza tres tipos de error para que todos los endpoints devuelvan
 * respuestas consistentes en formato ApiResponse, en lugar de dejar que
 * Spring devuelva sus páginas de error por defecto:
 *
 *  1. Validaciones de campos (@Valid) que fallan -> 400 Bad Request
 *  2. Correo ya registrado -> 409 Conflict
 *  3. Credenciales incorrectas en el login -> 401 Unauthorized
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Se dispara cuando @Valid detecta que un RegistroRequest o
    // LoginRequest no cumple las anotaciones de validación
    // (@NotBlank, @Email, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse(false, mensaje));
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiResponse> manejarCorreoDuplicado(CorreoDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, ex.getMessage()));
    }
}
