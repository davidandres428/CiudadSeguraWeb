package com.ciudadsegura.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteIncidenteRepositoryCustom {

    List<Object[]> contarPorEstado(LocalDateTime desde, LocalDateTime hasta);

    List<Object[]> contarPorPrioridad(LocalDateTime desde, LocalDateTime hasta);

    List<Object[]> contarPorTipo(LocalDateTime desde, LocalDateTime hasta);
}
