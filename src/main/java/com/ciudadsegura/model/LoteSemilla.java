package com.ciudadsegura.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_semilla")
public class LoteSemilla {

    @Id
    @Column(name = "clave", length = 80)
    private String clave;

    @Column(name = "fecha_ejecucion", nullable = false, updatable = false)
    private LocalDateTime fechaEjecucion;

    protected LoteSemilla() {
    }

    public LoteSemilla(String clave) {
        this.clave = clave;
    }

    @PrePersist
    void prePersist() {
        if (fechaEjecucion == null) {
            fechaEjecucion = LocalDateTime.now();
        }
    }
}
