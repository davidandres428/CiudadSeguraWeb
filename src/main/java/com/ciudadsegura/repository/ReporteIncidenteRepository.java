package com.ciudadsegura.repository;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteIncidenteRepository extends JpaRepository<ReporteIncidente, Long> {

    List<ReporteIncidente> findByUsuarioId(Long usuarioId);

    List<ReporteIncidente> findByEstado(EstadoReporte estado);
}
