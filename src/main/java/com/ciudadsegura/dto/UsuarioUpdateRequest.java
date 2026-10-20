package com.ciudadsegura.dto;

import jakarta.validation.constraints.Email;
import com.ciudadsegura.model.enums.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {

    private RolUsuario rol;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    public RolUsuario getRol() { return rol; }

    public void setRol(RolUsuario rol) { this.rol = rol; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

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
}
