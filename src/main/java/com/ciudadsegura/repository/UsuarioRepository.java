package com.ciudadsegura.repository;

import com.ciudadsegura.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para operaciones CRUD sobre la entidad Usuario.
 * Spring Data JPA genera automáticamente la implementación.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}