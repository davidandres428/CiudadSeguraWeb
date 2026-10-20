package com.ciudadsegura.service;

import com.ciudadsegura.dto.UsuarioUpdateRequest;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    public Usuario actualizar(Long id, UsuarioUpdateRequest datos) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombre(datos.getNombre());
        if (!usuario.getEmail().equalsIgnoreCase(datos.getEmail())
                && usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + datos.getEmail());
        }
        usuario.setEmail(datos.getEmail());
        if (datos.getRol() != null) {
            usuario.setRol(datos.getRol());
        }
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
