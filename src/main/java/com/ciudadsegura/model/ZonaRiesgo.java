package com.ciudadsegura.model;

import com.ciudadsegura.model.enums.NivelRiesgo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDate;

@Entity
@Table(name = "zonas_riesgo")
public class ZonaRiesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "El nivel de riesgo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false)
    private NivelRiesgo nivelRiesgo;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    @Column(nullable = false)
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    @Column(nullable = false)
    private Double longitud;

    @Column(name = "estado_camaras", length = 80)
    private String estadoCamaras;

    @Column(name = "ultima_inspeccion")
    private LocalDate ultimaInspeccion;

    public ZonaRiesgo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getEstadoCamaras() {
        return estadoCamaras;
    }

    public void setEstadoCamaras(String estadoCamaras) {
        this.estadoCamaras = estadoCamaras;
    }

    public LocalDate getUltimaInspeccion() {
        return ultimaInspeccion;
    }

    public void setUltimaInspeccion(LocalDate ultimaInspeccion) {
        this.ultimaInspeccion = ultimaInspeccion;
    }
}
