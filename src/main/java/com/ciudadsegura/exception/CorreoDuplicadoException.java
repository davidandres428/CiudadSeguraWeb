package com.ciudadsegura.exception;

/**
 * Excepción de negocio lanzada cuando se intenta registrar un usuario
 * con un correo electrónico que ya existe en la base de datos.
 */
public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
