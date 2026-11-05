package com.ciudadsegura.controller;

import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.service.ZonaRiesgoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas-riesgo")
public class ZonaRiesgoController {

    private final ZonaRiesgoService zonaRiesgoService;

    public ZonaRiesgoController(ZonaRiesgoService zonaRiesgoService) {
        this.zonaRiesgoService = zonaRiesgoService;
    }

    @GetMapping
    public List<ZonaRiesgo> listar(@RequestParam(required = false) NivelRiesgo nivel) {
        if (nivel != null) {
            return zonaRiesgoService.listarPorNivel(nivel);
        }
        return zonaRiesgoService.listarTodas();
    }

    @GetMapping("/count")
    public long contar() {
        return zonaRiesgoService.contar();
    }

    @GetMapping("/{id}")
    public ZonaRiesgo buscarPorId(@PathVariable Long id) {
        return zonaRiesgoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ZonaRiesgo> crear(
            @Valid @RequestBody ZonaRiesgo zona,
            @AuthenticationPrincipal Jwt jwt) {
        ZonaRiesgo creada = zonaRiesgoService.crear(zona, obtenerEmailAutenticado(jwt));
        return ResponseEntity.created(java.net.URI.create("/api/zonas-riesgo/" + creada.getId())).body(creada);
    }

    @PutMapping("/{id}")
    public ZonaRiesgo actualizar(@PathVariable Long id, @Valid @RequestBody ZonaRiesgo zona) {
        return zonaRiesgoService.actualizar(id, zona);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        zonaRiesgoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private String obtenerEmailAutenticado(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No se pudo identificar al usuario autenticado.");
        }
        return jwt.getSubject();
    }
}
