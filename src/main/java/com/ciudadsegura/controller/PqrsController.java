package com.ciudadsegura.controller;

import com.ciudadsegura.dto.PqrsRequest;
import com.ciudadsegura.dto.PqrsResponse;
import com.ciudadsegura.service.PqrsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pqrs")
public class PqrsController {

    private final PqrsService pqrsService;

    public PqrsController(PqrsService pqrsService) {
        this.pqrsService = pqrsService;
    }

    @PostMapping
    public ResponseEntity<PqrsResponse> radicar(@Valid @RequestBody PqrsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PqrsResponse(pqrsService.radicar(request)));
    }
}
