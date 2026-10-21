package com.ciudadsegura.repository;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReporteIncidenteRepository extends JpaRepository<ReporteIncidente, Long>,
        JpaSpecificationExecutor<ReporteIncidente>, ReporteIncidenteRepositoryCustom {

    List<ReporteIncidente> findByUsuarioId(Long usuarioId);

    List<ReporteIncidente> findByEstado(EstadoReporte estado);
}
