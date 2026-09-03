package com.ciudadsegura.controller;

import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<Alerta> listar(@RequestParam(required = false) Boolean activas) {
        if (Boolean.TRUE.equals(activas)) {
            return alertaService.listarActivas();
        }
        return alertaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Alerta buscarPorId(@PathVariable Long id) {
        return alertaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Alerta> crear(@Valid @RequestBody Alerta alerta,
                                         @RequestParam(required = false) Long zonaRiesgoId) {
        Alerta creada = alertaService.crear(alerta, zonaRiesgoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public Alerta actualizar(@PathVariable Long id, @Valid @RequestBody Alerta alerta,
                              @RequestParam(required = false) Long zonaRiesgoId) {
        return alertaService.actualizar(id, alerta, zonaRiesgoId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alertaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
