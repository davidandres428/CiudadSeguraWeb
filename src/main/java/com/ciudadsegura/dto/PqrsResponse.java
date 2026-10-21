package com.ciudadsegura.dto;

import com.ciudadsegura.model.RadicacionPqrs;
import com.ciudadsegura.model.enums.EstadoPqrs;

import java.time.LocalDateTime;

public record PqrsResponse(Long id, EstadoPqrs estado, LocalDateTime fechaRadicacion) {

    public PqrsResponse(RadicacionPqrs radicacion) {
        this(radicacion.getId(), radicacion.getEstado(), radicacion.getFechaRadicacion());
    }
}
