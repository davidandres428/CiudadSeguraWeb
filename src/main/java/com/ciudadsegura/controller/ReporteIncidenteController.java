package com.ciudadsegura.controller;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.service.ReporteIncidenteService;
import jakarta.validation.Valid;
import com.ciudadsegura.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal UsuarioAutenticado yo,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoReporte estado) {
        if (!yo.esAdmin()) {
            // Un ciudadano solo ve sus propios reportes.
            List<ReporteIncidente> propios = reporteService.listarPorUsuario(yo.id());
            return estado == null ? propios
                    : propios.stream().filter(r -> r.getEstado() == estado).toList();
        }
        if (usuarioId != null) {
            return reporteService.listarPorUsuario(usuarioId);
        }
        if (estado != null) {
            return reporteService.listarPorEstado(estado);
        }
        return reporteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ReporteIncidente buscarPorId(@AuthenticationPrincipal UsuarioAutenticado yo, @PathVariable Long id) {
        return propio(yo, id);
    }

    @PostMapping
    public ResponseEntity<ReporteIncidente> crear(@AuthenticationPrincipal UsuarioAutenticado yo,
                                                    @Valid @RequestBody ReporteIncidente reporte) {
        ReporteIncidente creado = reporteService.crear(reporte, yo.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ReporteIncidente actualizar(@AuthenticationPrincipal UsuarioAutenticado yo, @PathVariable Long id,
                                       @Valid @RequestBody ReporteIncidente reporte) {
        propio(yo, id);
        if (!yo.esAdmin()) {
            reporte.setEstado(null); // solo un administrador cambia el estado
        }
        return reporteService.actualizar(id, reporte);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioAutenticado yo, @PathVariable Long id) {
        propio(yo, id);
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** Devuelve el reporte si es del usuario autenticado o si este es administrador; si no, 403. */
    private ReporteIncidente propio(UsuarioAutenticado yo, Long id) {
        ReporteIncidente reporte = reporteService.buscarPorId(id);
        if (!yo.esAdmin() && !reporte.getUsuario().getId().equals(yo.id())) {
            throw new AccessDeniedException("No tienes permiso sobre este reporte");
        }
        return reporte;
    }
}
