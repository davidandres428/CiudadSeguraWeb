package com.ciudadsegura.controller;

import com.ciudadsegura.dto.ReporteMetricasResponse;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.service.ReporteIncidenteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.listarFiltrados(usuarioId, estado, desde, hasta);
    }

    @GetMapping("/metricas")
    public ReporteMetricasResponse obtenerMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.obtenerMetricas(desde, hasta);
    }

    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<ReporteIncidente> reportes = reporteService.listarFiltrados(null, estado, desde, hasta);
        byte[] contenido = reporteService.exportarCsv(reportes).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reportes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(contenido);
    }

    @GetMapping("/{id}")
    public ReporteIncidente buscarPorId(@PathVariable Long id) {
        return reporteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ReporteIncidente> crear(@Valid @RequestBody ReporteIncidente reporte,
                                                   @AuthenticationPrincipal Jwt jwt) {
        ReporteIncidente creado = reporteService.crear(reporte, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ReporteIncidente actualizar(@PathVariable Long id, @Valid @RequestBody ReporteIncidente reporte,
                                        @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return reporteService.actualizar(id, reporte, jwt.getSubject(), esAdministrador(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                          Authentication authentication) {
        reporteService.eliminar(id, jwt.getSubject(), esAdministrador(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "SCOPE_ADMINISTRADOR".equals(authority.getAuthority()));
    }
}
