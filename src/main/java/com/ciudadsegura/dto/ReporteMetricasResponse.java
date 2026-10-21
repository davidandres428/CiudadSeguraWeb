package com.ciudadsegura.dto;

import java.util.Map;

public class ReporteMetricasResponse {

    private final long total;
    private final Map<String, Long> porEstado;
    private final Map<String, Long> porPrioridad;
    private final Map<String, Long> porTipo;

    public ReporteMetricasResponse(
            long total,
            Map<String, Long> porEstado,
            Map<String, Long> porPrioridad,
            Map<String, Long> porTipo) {
        this.total = total;
        this.porEstado = porEstado;
        this.porPrioridad = porPrioridad;
        this.porTipo = porTipo;
    }

    public long getTotal() {
        return total;
    }

    public Map<String, Long> getPorEstado() {
        return porEstado;
    }

    public Map<String, Long> getPorPrioridad() {
        return porPrioridad;
    }

    public Map<String, Long> getPorTipo() {
        return porTipo;
    }
}
