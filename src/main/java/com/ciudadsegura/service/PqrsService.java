package com.ciudadsegura.service;

import com.ciudadsegura.dto.PqrsRequest;
import com.ciudadsegura.model.RadicacionPqrs;
import com.ciudadsegura.repository.RadicacionPqrsRepository;
import org.springframework.stereotype.Service;

@Service
public class PqrsService {

    private final RadicacionPqrsRepository repository;

    public PqrsService(RadicacionPqrsRepository repository) {
        this.repository = repository;
    }

    public RadicacionPqrs radicar(PqrsRequest request) {
        RadicacionPqrs radicacion = new RadicacionPqrs(
                request.nombre().trim(),
                request.email().trim(),
                request.tipo(),
                request.asunto().trim(),
                request.detalle().trim());
        return repository.save(radicacion);
    }
}
