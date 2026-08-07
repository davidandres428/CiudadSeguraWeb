package com.ciudadsegura.repository;

import com.ciudadsegura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD sobre la entidad Usuario.
 * Spring Data JPA genera automáticamente la implementación.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por su correo electrónico (usado en login y
    // en la validación de correo duplicado durante el registro)
    Optional<Usuario> findByEmail(String email);

    // Indica si ya existe un usuario registrado con ese correo
    boolean existsByEmail(String email);
}