package com.ciudadsegura.exception;

/**
 * Excepción de negocio lanzada cuando el correo no existe o la
 * contraseña no coincide durante el inicio de sesión.
 * Se maneja de forma centralizada en GlobalExceptionHandler.
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
