package com.ciudadsegura.service;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    public static final String ADMINISTRADOR_PRINCIPAL = "david.andresgalvis@gmail.com";

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(RegistroRequest request) {
        if (ADMINISTRADOR_PRINCIPAL.equalsIgnoreCase(request.getEmail().trim())) {
            throw new EmailDuplicadoException(
                    "Este correo está reservado para la cuenta de administración principal.");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + request.getEmail());
        }
        String hash = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail(), hash);
        usuario.setRol(RolUsuario.CIUDADANO);
        return usuarioRepository.save(usuario);
    }

    public Usuario login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }
        if (ADMINISTRADOR_PRINCIPAL.equalsIgnoreCase(usuario.getEmail())
                && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            usuario.setRol(RolUsuario.ADMINISTRADOR);
            usuario = usuarioRepository.save(usuario);
        }
        return usuario;
    }

    public Usuario loginSocial(String email, String nombre, boolean correoVerificado) {
        if (email == null || email.isBlank()) {
            throw new CredencialesInvalidasException(
                    "El proveedor social no compartió un correo electrónico.");
        }
        String correo = email.trim().toLowerCase(Locale.ROOT);
        boolean administradorPrincipal =
                ADMINISTRADOR_PRINCIPAL.equalsIgnoreCase(correo);
        if (administradorPrincipal && !correoVerificado) {
            throw new CredencialesInvalidasException(
                    "La cuenta principal requiere un correo verificado por el proveedor.");
        }

        Usuario usuario = usuarioRepository.findByEmail(correo).orElseGet(() -> {
            Usuario nuevo = new Usuario(
                    nombre == null || nombre.isBlank() ? correo : nombre.trim(),
                    correo,
                    passwordEncoder.encode(UUID.randomUUID().toString()));
            nuevo.setRol(administradorPrincipal ? RolUsuario.ADMINISTRADOR : RolUsuario.CIUDADANO);
            return usuarioRepository.save(nuevo);
        });
        if (administradorPrincipal && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            usuario.setRol(RolUsuario.ADMINISTRADOR);
        }
        return usuarioRepository.save(usuario);
    }
}
