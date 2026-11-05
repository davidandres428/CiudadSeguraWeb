package com.ciudadsegura.service;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    public static final String ADMINISTRADOR_PRINCIPAL = "david.andresgalvis@gmail.com";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String correo = normalizarCorreo(request.getEmail());
        if (ADMINISTRADOR_PRINCIPAL.equals(correo) || usuarioRepository.existsByEmail(correo)) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + correo);
        }

        String hash = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(request.getNombre(), correo, hash);
        // El registro es público: el rol recibido en el DTO nunca concede privilegios.
        usuario.setRol(RolUsuario.CIUDADANO);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarCorreo(request.getEmail()))
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

    @Transactional
    public Usuario loginSocial(String email, String nombre, boolean correoVerificado) {
        if (email == null || email.isBlank()) {
            throw new CredencialesInvalidasException("El proveedor social no compartió un correo electrónico.");
        }
        String correo = normalizarCorreo(email);
        boolean esAdministradorPrincipal = ADMINISTRADOR_PRINCIPAL.equals(correo);
        if (esAdministradorPrincipal && !correoVerificado) {
            throw new CredencialesInvalidasException("El proveedor social no verificó el correo electrónico.");
        }

        Usuario existente = usuarioRepository.findByEmail(correo).orElse(null);
        if (existente != null) {
            if (esAdministradorPrincipal
                    && existente.getRol() != RolUsuario.ADMINISTRADOR) {
                existente.setRol(RolUsuario.ADMINISTRADOR);
                return usuarioRepository.save(existente);
            }
            return existente;
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre == null || nombre.isBlank() ? correo : nombre.trim());
        nuevo.setEmail(correo);
        nuevo.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        nuevo.setRol(esAdministradorPrincipal
                ? RolUsuario.ADMINISTRADOR
                : RolUsuario.CIUDADANO);
        return usuarioRepository.save(nuevo);
    }

    private String normalizarCorreo(String email) {
        if (email == null) {
            throw new CredencialesInvalidasException("El correo electrónico es obligatorio.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}