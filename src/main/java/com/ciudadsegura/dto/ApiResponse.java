package com.ciudadsegura.dto;

/**
 * Envoltorio genérico de respuesta para los servicios de autenticación.
 * Permite devolver, de forma consistente, un indicador de éxito, un
 * mensaje legible para el usuario y (opcionalmente) los datos del
 * usuario autenticado o registrado.
 */
public class ApiResponse {

    private boolean exito;
    private String mensaje;
    private UsuarioResponse usuario;

    public ApiResponse(boolean exito, String mensaje, UsuarioResponse usuario) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public ApiResponse(boolean exito, String mensaje) {
        this(exito, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}
