package com.ciudadsegura.model;

import com.ciudadsegura.model.enums.EstadoPqrs;
import com.ciudadsegura.model.enums.TipoPqrs;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "radicaciones_pqrs")
public class RadicacionPqrs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPqrs tipo;

    @Column(nullable = false, length = 160)
    private String asunto;

    @Column(nullable = false, length = 4000)
    private String detalle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPqrs estado = EstadoPqrs.RADICADA;

    @Column(name = "fecha_radicacion", nullable = false, updatable = false)
    private LocalDateTime fechaRadicacion;

    protected RadicacionPqrs() {
    }

    public RadicacionPqrs(String nombre, String email, TipoPqrs tipo, String asunto, String detalle) {
        this.nombre = nombre;
        this.email = email;
        this.tipo = tipo;
        this.asunto = asunto;
        this.detalle = detalle;
    }

    @PrePersist
    void prePersist() {
        this.fechaRadicacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPqrs.RADICADA;
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public TipoPqrs getTipo() {
        return tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getDetalle() {
        return detalle;
    }

    public EstadoPqrs getEstado() {
        return estado;
    }

    public LocalDateTime getFechaRadicacion() {
        return fechaRadicacion;
    }
}
