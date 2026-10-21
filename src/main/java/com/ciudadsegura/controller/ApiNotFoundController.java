package com.ciudadsegura.controller;

import com.ciudadsegura.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/**")
public class ApiNotFoundController {

    @RequestMapping
    public ResponseEntity<ErrorResponse> rutaApiNoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Endpoint API no encontrado."));
    }
}
