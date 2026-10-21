package com.ciudadsegura.dto;

public class AuthResponse {

    private final String accessToken;
    private final UsuarioResponse usuario;

    public AuthResponse(String accessToken, UsuarioResponse usuario) {
        this.accessToken = accessToken;
        this.usuario = usuario;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}
