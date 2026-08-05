package com.ciudadsegura.controller;

import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar los usuarios (ciudadanos) del sistema.
 * Expone endpoints CRUD sobre el recurso /api/usuarios.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // GET /api/usuarios -> lista todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // GET /api/usuarios/{id} -> obtiene un usuario por id
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/usuarios -> crea un nuevo usuario
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // PUT /api/usuarios/{id} -> actualiza un usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id,
                                                       @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datosActualizados.getNombre());
            usuario.setEmail(datosActualizados.getEmail());
            usuario.setPassword(datosActualizados.getPassword());
            usuario.setTelefono(datosActualizados.getTelefono());
            Usuario actualizado = usuarioRepository.save(usuario);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
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