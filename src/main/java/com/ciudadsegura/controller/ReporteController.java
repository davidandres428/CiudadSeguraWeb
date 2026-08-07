package com.ciudadsegura.controller;

import com.ciudadsegura.model.Reporte;
import com.ciudadsegura.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar los reportes de incidentes ciudadanos.
 * Expone endpoints CRUD sobre el recurso /api/reportes.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteRepository reporteRepository;

    // GET /api/reportes -> lista todos los reportes
    @GetMapping
    public List<Reporte> listarReportes() {
        return reporteRepository.findAll();
    }

    // GET /api/reportes/{id} -> obtiene un reporte por id
    @GetMapping("/{id}")
    public ResponseEntity<Reporte> obtenerReporte(@PathVariable Long id) {
        Optional<Reporte> reporte = reporteRepository.findById(id);
        return reporte.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/reportes -> crea un nuevo reporte
    @PostMapping
    public Reporte crearReporte(@RequestBody Reporte reporte) {
        return reporteRepository.save(reporte);
    }

    // PUT /api/reportes/{id} -> actualiza un reporte existente
    @PutMapping("/{id}")
    public ResponseEntity<Reporte> actualizarReporte(@PathVariable Long id,
                                                       @RequestBody Reporte datosActualizados) {
        return reporteRepository.findById(id).map(reporte -> {
            reporte.setTitulo(datosActualizados.getTitulo());
            reporte.setDescripcion(datosActualizados.getDescripcion());
            reporte.setCategoria(datosActualizados.getCategoria());
            reporte.setUbicacion(datosActualizados.getUbicacion());
            reporte.setEstado(datosActualizados.getEstado());
            Reporte actualizado = reporteRepository.save(reporte);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/reportes/{id} -> elimina un reporte
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Long id) {
        if (!reporteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reporteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}