package com.ciudadsegura.repository;

import com.ciudadsegura.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para operaciones CRUD sobre la entidad Reporte.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // Consulta personalizada: buscar reportes por estado
    List<Reporte> findByEstado(String estado);

    // Consulta personalizada: buscar reportes por categoría
    List<Reporte> findByCategoria(String categoria);
}