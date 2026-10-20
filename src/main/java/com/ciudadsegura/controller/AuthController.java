package com.ciudadsegura.controller;

import com.ciudadsegura.dto.AuthResponse;
import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.security.JwtService;
import com.ciudadsegura.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping({"/registro", "/register"})
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta(authService.registrar(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(respuesta(authService.login(request)));
    }

    private AuthResponse respuesta(Usuario usuario) {
        return new AuthResponse(jwtService.generar(usuario), new UsuarioResponse(usuario));
    }
}
