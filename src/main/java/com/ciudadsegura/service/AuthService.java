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

    public Usuario registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + request.getEmail());
        }
        
        String hash = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail(), hash);
        
        // Si es el primer usuario de la base de datos, se convierte en Administrador automáticamente
        if (usuarioRepository.count() == 0) {
            usuario.setRol(RolUsuario.ADMINISTRADOR);
        } else {
            usuario.setRol(RolUsuario.CIUDADANO);
        }
        
        return usuarioRepository.save(usuario);
    }

    public Usuario login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        return usuario;
    }

    public Usuario loginSocial(String email, String nombre, boolean correoVerificado) {
        if (email == null || email.isBlank()) {
            throw new CredencialesInvalidasException("El proveedor social no compartió un correo electrónico.");
        }

        String correo = email.trim().toLowerCase(Locale.ROOT);
        
        return usuarioRepository.findByEmail(correo).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre == null || nombre.isBlank() ? correo : nombre.trim());
            nuevo.setEmail(correo);
            nuevo.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            
            if (usuarioRepository.count() == 0) {
                nuevo.setRol(RolUsuario.ADMINISTRADOR);
            } else {
                nuevo.setRol(RolUsuario.CIUDADANO);
            }
            return usuarioRepository.save(nuevo);
        });
    }
}