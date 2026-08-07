package com.ciudadsegura.controller;

import com.ciudadsegura.dto.ApiResponse;
import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST de autenticación.
 *
 * Expone los dos servicios web pedidos por la evidencia GA7-220501096-AA5-EV01:
 *  - POST /api/auth/registro : servicio para el registro de un nuevo ciudadano
 *  - POST /api/auth/login    : servicio para el inicio de sesión
 *
 * La anotación @Valid activa las validaciones definidas en RegistroRequest
 * y LoginRequest (@NotBlank, @Email, @Size). Si alguna falla, Spring lanza
 * MethodArgumentNotValidException, que es capturada por
 * GlobalExceptionHandler y traducida a una respuesta clara para el cliente.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/registro -> crea un nuevo usuario (ciudadano)
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        ApiResponse respuesta = authService.registrar(request);
        return ResponseEntity.ok(respuesta);
    }

    // POST /api/auth/login -> valida credenciales y autentica al usuario
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        ApiResponse respuesta = authService.iniciarSesion(request);
        return ResponseEntity.ok(respuesta);
    }
}
