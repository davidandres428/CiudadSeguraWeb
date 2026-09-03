package com.ciudadsegura.controller;

import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.service.ZonaRiesgoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ZonaRiesgo buscarPorId(@PathVariable Long id) {
        return zonaRiesgoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ZonaRiesgo> crear(@Valid @RequestBody ZonaRiesgo zona) {
        ZonaRiesgo creada = zonaRiesgoService.crear(zona);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
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
}
