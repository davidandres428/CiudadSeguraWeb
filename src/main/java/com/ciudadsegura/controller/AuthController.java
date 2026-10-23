package com.ciudadsegura.controller;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.dto.AuthResponse;
import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.service.AuthService;
import com.ciudadsegura.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final String googleClientId;
    private final String facebookClientId;
    private final String googleClientSecret;
    private final String facebookClientSecret;
    private final String facebookGraphApiVersion;

    public AuthController(
            AuthService authService,
            JwtTokenService jwtTokenService,
            @Value("${oauth.google.client-id:}") String googleClientId,
            @Value("${oauth.google.client-secret:}") String googleClientSecret,
            @Value("${oauth.facebook.client-id:}") String facebookClientId,
            @Value("${oauth.facebook.client-secret:}") String facebookClientSecret,
            @Value("${oauth.facebook.graph-api-version:}") String facebookGraphApiVersion) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.facebookClientId = facebookClientId;
        this.facebookClientSecret = facebookClientSecret;
        this.facebookGraphApiVersion = facebookGraphApiVersion;
    }

    @PostMapping({"/registro", "/register"})
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(crearRespuesta(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = authService.login(request);
        return ResponseEntity.ok(crearRespuesta(usuario));
    }

    @GetMapping("/social-providers")
    public java.util.Map<String, Boolean> proveedoresSociales() {
        return java.util.Map.of(
                "google", !googleClientId.isBlank() && !googleClientSecret.isBlank(),
                "facebook", !facebookClientId.isBlank() && !facebookClientSecret.isBlank()
                        && facebookGraphApiVersion.matches("v\\d+\\.\\d+"),
                "instagram", false);
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(jwtTokenService.crearToken(usuario), new UsuarioResponse(usuario));
    }
}
