package com.ciudadsegura.controller;

import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST administrativo para consultar y eliminar usuarios.
 *
 * El registro y el inicio de sesion NO se manejan aqui: para eso existe
 * el servicio dedicado AuthController (/api/auth/registro y
 * /api/auth/login), que valida los datos y cifra la contrasena con
 * BCrypt. Este controlador solo expone operaciones de consulta y
 * administracion, y nunca devuelve la contrasena del usuario.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // GET /api/usuarios -> lista todos los usuarios (sin exponer la contrasena)
    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::new)
                .collect(Collectors.toList());
    }

    // GET /api/usuarios/{id} -> obtiene un usuario por id (sin la contrasena)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(u -> ResponseEntity.ok(new UsuarioResponse(u)))
                       .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/usuarios/{id} -> elimina un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
