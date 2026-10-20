package com.ciudadsegura.service;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicadoException("Ya existe un usuario registrado con el email " + request.getEmail());
        }
        String hash = passwordEncoder.encode(request.getPassword());
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail(), hash);
        // El registro público siempre crea ciudadanos; los administradores se asignan desde /api/usuarios.
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
}
