package com.ciudadsegura.controller;

import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.dto.UsuarioUpdateRequest;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    public UsuarioApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioResponse::new).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return new UsuarioResponse(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest datos) {
        return new UsuarioResponse(usuarioService.actualizar(id, datos));
    }

    @PutMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @RequestParam RolUsuario rol) {
        return new UsuarioResponse(usuarioService.cambiarRol(id, rol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
