package com.ciudadsegura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del cifrado de contraseñas.
 * BCrypt genera un hash distinto cada vez (incluye "salt" aleatorio),
 * por lo que nunca se guarda ni se compara la contraseña en texto plano.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
