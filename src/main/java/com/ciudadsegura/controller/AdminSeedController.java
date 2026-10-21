package com.ciudadsegura.controller;

import com.ciudadsegura.dto.SeedMasivoResponse;
import com.ciudadsegura.dto.ErrorResponse;
import com.ciudadsegura.service.SeedMasivoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminSeedController {

    private final SeedMasivoService seedMasivoService;

    public AdminSeedController(SeedMasivoService seedMasivoService) {
        this.seedMasivoService = seedMasivoService;
    }

    @PostMapping("/seed-masivo")
    public ResponseEntity<SeedMasivoResponse> sembrarDatosMasivos() {
        return ResponseEntity.ok(seedMasivoService.sembrar());
    }

    @GetMapping("/seed-masivo")
    public ResponseEntity<ErrorResponse> rechazarLecturaComoSembrado() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.ALLOW, "POST")
                .body(new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Este endpoint modifica datos; utiliza POST para ejecutarlo."));
    }
}
