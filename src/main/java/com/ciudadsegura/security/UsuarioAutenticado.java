package com.ciudadsegura.security;

/** Principal que se guarda en el contexto de seguridad a partir del JWT. */
public record UsuarioAutenticado(Long id, String email, String rol) {

    public boolean esAdmin() {
        return "ADMINISTRADOR".equals(rol);
    }
}
