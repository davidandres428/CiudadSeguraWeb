package com.ciudadsegura.service;

import com.ciudadsegura.dto.*;
import com.ciudadsegura.exception.CorreoDuplicadoException;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación: contiene la lógica de negocio para el
 * registro de nuevos ciudadanos y el inicio de sesión, de forma que
 * el controlador (AuthController) quede simple y solo se encargue de
 * exponer los endpoints HTTP.
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo ciudadano.
     * Valida que el correo no esté previamente registrado y cifra la
     * contraseña con BCrypt antes de guardarla.
     *
     * @param request datos de registro ya validados por Bean Validation
     * @return ApiResponse con el usuario creado (sin la contraseña)
     * @throws CorreoDuplicadoException si el correo ya existe
     */
    public ApiResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new CorreoDuplicadoException(
                    "Ya existe una cuenta registrada con el correo " + request.getEmail());
        }

        Usuario usuario = new Usuario(
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getTelefono()
        );

        Usuario guardado = usuarioRepository.save(usuario);

        return new ApiResponse(
                true,
                "Usuario registrado exitosamente",
                new UsuarioResponse(guardado)
        );
    }

    /**
     * Autentica a un ciudadano verificando su correo y contraseña.
     *
     * Por seguridad, cuando el correo no existe o la contraseña no
     * coincide se devuelve siempre el mismo mensaje genérico
     * ("Correo o contraseña incorrectos"), para no revelar a un
     * atacante si el correo está o no registrado en el sistema.
     *
     * @param request credenciales enviadas por el cliente
     * @return ApiResponse con el usuario autenticado
     * @throws CredencialesInvalidasException si el correo o la contraseña no son correctos
     */
    public ApiResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        boolean coincide = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        if (!coincide) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }

        return new ApiResponse(
                true,
                "Autenticación exitosa",
                new UsuarioResponse(usuario)
        );
    }
}
