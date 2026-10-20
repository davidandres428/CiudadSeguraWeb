package com.ciudadsegura.dto;

import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;

import java.time.LocalDateTime;

/**
 * DTO de salida para no exponer nunca la contraseña del usuario.
 */
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private LocalDateTime fechaRegistro;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
        this.fechaRegistro = usuario.getFechaRegistro();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
