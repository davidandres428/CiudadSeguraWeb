package com.ciudadsegura.controller;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/registro", "/register"})
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponse(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = authService.login(request);
        return ResponseEntity.ok(new UsuarioResponse(usuario));
    }
}
