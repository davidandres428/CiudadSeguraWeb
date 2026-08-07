package com.ciudadsegura.dto;

import com.ciudadsegura.model.Usuario;

/**
 * Representación segura de un Usuario para devolver en las respuestas
 * de la API. Nunca incluye la contraseña (ni siquiera cifrada), para
 * no exponer ese dato sensible al cliente.
 */
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
