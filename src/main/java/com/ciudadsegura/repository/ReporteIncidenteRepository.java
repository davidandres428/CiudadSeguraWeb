package com.ciudadsegura.repository;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteIncidenteRepository extends JpaRepository<ReporteIncidente, Long> {

    List<ReporteIncidente> findByUsuarioId(Long usuarioId);

    List<ReporteIncidente> findByEstado(EstadoReporte estado);

    @Query("""
            select r from ReporteIncidente r
            where (:usuarioId is null or r.usuario.id = :usuarioId)
              and (:estado is null or r.estado = :estado)
              and (:desde is null or r.fechaReporte >= :desde)
              and (:hasta is null or r.fechaReporte < :hasta)
            order by r.fechaReporte desc
            """)
    List<ReporteIncidente> buscarFiltrados(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoReporte estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
            select r.estado, count(r) from ReporteIncidente r
            where (:desde is null or r.fechaReporte >= :desde)
              and (:hasta is null or r.fechaReporte < :hasta)
            group by r.estado
            """)
    List<Object[]> contarPorEstado(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("""
            select r.prioridad, count(r) from ReporteIncidente r
            where (:desde is null or r.fechaReporte >= :desde)
              and (:hasta is null or r.fechaReporte < :hasta)
            group by r.prioridad
            """)
    List<Object[]> contarPorPrioridad(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("""
            select r.tipo, count(r) from ReporteIncidente r
            where (:desde is null or r.fechaReporte >= :desde)
              and (:hasta is null or r.fechaReporte < :hasta)
            group by r.tipo
            """)
    List<Object[]> contarPorTipo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
