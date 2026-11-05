package com.ciudadsegura.service;

import com.ciudadsegura.dto.UsuarioUpdateRequest;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + usuario.getEmail());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getRol() == null) {
            usuario.setRol(RolUsuario.CIUDADANO);
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioUpdateRequest datos) {
        Usuario usuario = buscarPorId(id);
        protegerAdministradorPrincipal(usuario);

        if (datos.getEmail() != null && !datos.getEmail().equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + datos.getEmail());
        }

        if (datos.getNombre() != null) {
            usuario.setNombre(datos.getNombre());
        }
        if (datos.getEmail() != null) {
            usuario.setEmail(datos.getEmail());
        }
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        if (datos.getRol() != null && datos.getRol() != usuario.getRol()) {
            validarCambioDeRol(usuario, datos.getRol());
            usuario.setRol(datos.getRol());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cambiarRol(Long id, RolUsuario rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        Usuario usuario = buscarPorId(id);
        protegerAdministradorPrincipal(usuario);
        if (rol != usuario.getRol()) {
            validarCambioDeRol(usuario, rol);
            usuario.setRol(rol);
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        protegerAdministradorPrincipal(usuario);
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR
                && usuarioRepository.countByRol(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede eliminar al último administrador");
        }
        usuarioRepository.delete(usuario);
    }

    private void protegerAdministradorPrincipal(Usuario usuario) {
        if (AuthService.ADMINISTRADOR_PRINCIPAL.equalsIgnoreCase(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La cuenta del administrador principal no se puede modificar");
        }
    }

    private void validarCambioDeRol(Usuario usuario, RolUsuario nuevoRol) {
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR && nuevoRol != RolUsuario.ADMINISTRADOR
                && usuarioRepository.countByRol(RolUsuario.ADMINISTRADOR) <= 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No se puede quitar el rol al último administrador");
        }
    }
}
