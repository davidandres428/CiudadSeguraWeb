package com.ciudadsegura.config;

import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador al arrancar si se definen ADMIN_EMAIL y ADMIN_PASSWORD
 * y ese email aún no existe. Sin estas variables no hace nada.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminInicial(UsuarioRepository repo, PasswordEncoder encoder,
                        @Value("${app.admin.email:}") String email,
                        @Value("${app.admin.password:}") String password) {
        this.repo = repo;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.length() < 6 || repo.existsByEmail(email)) {
            return;
        }
        Usuario admin = new Usuario("Administrador", email, encoder.encode(password));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        repo.save(admin);
    }
}
