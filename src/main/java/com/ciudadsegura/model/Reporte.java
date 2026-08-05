package com.ciudadsegura.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un reporte o denuncia de incidente
 * hecho por un ciudadano.
 */
@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String categoria; // Ej: robo, vandalismo, accidente, sospechoso

    @Column(nullable = false, length = 200)
    private String ubicacion;

    @Column(name = "fecha_reporte", nullable = false)
private LocalDateTime fechaReporte = LocalDateTime.now();

    @Column(nullable = false, length = 20)
private String estado = "pendiente"; // Ej: pendiente, en_revision, resuelto

    // Relación: un reporte pertenece a un usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Constructor vacío requerido por JPA
    public Reporte() {
    }

    public Reporte(String titulo, String descripcion, String categoria,
                    String ubicacion, Usuario usuario) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.usuario = usuario;
        this.fechaReporte = LocalDateTime.now();
        this.estado = "pendiente";
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}