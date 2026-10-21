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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
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

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(jwtTokenService.crearToken(usuario), new UsuarioResponse(usuario));
    }
}
