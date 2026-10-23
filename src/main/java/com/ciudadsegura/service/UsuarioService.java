package com.ciudadsegura.service;

import com.ciudadsegura.dto.UsuarioUpdateRequest;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        protegerCuentaPrincipal(usuario);
        usuario.setNombre(datos.getNombre());
        if (!usuario.getEmail().equalsIgnoreCase(datos.getEmail())
                && usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + datos.getEmail());
        }
        usuario.setEmail(datos.getEmail());
        if (datos.getRol() != null) {
            cambiarRol(usuario, datos.getRol());
        }
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        protegerCuentaPrincipal(usuario);
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR
                && usuarioRepository.countByRol(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar el último administrador.");
        }
        usuarioRepository.delete(usuario);
    }

    private void cambiarRol(Usuario usuario, RolUsuario rol) {
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio.");
        }
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR && rol != RolUsuario.ADMINISTRADOR
                && usuarioRepository.countByRol(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede quitar el rol al último administrador.");
        }
        usuario.setRol(rol);
    }

    private void protegerCuentaPrincipal(Usuario usuario) {
        if (AuthService.ADMINISTRADOR_PRINCIPAL.equalsIgnoreCase(usuario.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "La cuenta de administración principal no se puede modificar ni eliminar.");
        }
    }
}
