package com.ciudadsegura.controller;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.service.ReporteIncidenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteIncidenteController {

    private final ReporteIncidenteService reporteService;

    public ReporteIncidenteController(ReporteIncidenteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public List<ReporteIncidente> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoReporte estado) {
        if (usuarioId != null) {
            return reporteService.listarPorUsuario(usuarioId);
        }
        if (estado != null) {
            return reporteService.listarPorEstado(estado);
        }
        return reporteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ReporteIncidente buscarPorId(@PathVariable Long id) {
        return reporteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ReporteIncidente> crear(@Valid @RequestBody ReporteIncidente reporte,
                                                    @RequestParam Long usuarioId) {
        ReporteIncidente creado = reporteService.crear(reporte, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ReporteIncidente actualizar(@PathVariable Long id, @Valid @RequestBody ReporteIncidente reporte) {
        return reporteService.actualizar(id, reporte);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
