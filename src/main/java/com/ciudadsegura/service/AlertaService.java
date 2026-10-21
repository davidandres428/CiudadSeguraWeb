package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final ZonaRiesgoRepository zonaRiesgoRepository;

    public AlertaService(AlertaRepository alertaRepository, ZonaRiesgoRepository zonaRiesgoRepository) {
        this.alertaRepository = alertaRepository;
        this.zonaRiesgoRepository = zonaRiesgoRepository;
    }

    public List<Alerta> listarTodas() {
        return alertaRepository.findAll();
    }

    public long contar() {
        return alertaRepository.count();
    }

    public long contarActivas() {
        return alertaRepository.countByActivaTrue();
    }

    public List<Alerta> listarActivas() {
        return alertaRepository.findByActivaTrue();
    }

    public Alerta buscarPorId(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada con id " + id));
    }

    public Alerta crear(Alerta alerta, Long zonaRiesgoId) {
        if (zonaRiesgoId != null) {
            alerta.setZonaRiesgo(buscarZona(zonaRiesgoId));
        }
        return alertaRepository.save(alerta);
    }

    public Alerta actualizar(Long id, Alerta datos, Long zonaRiesgoId) {
        Alerta alerta = buscarPorId(id);
        alerta.setTitulo(datos.getTitulo());
        alerta.setMensaje(datos.getMensaje());
        alerta.setTipo(datos.getTipo());
        alerta.setActiva(datos.isActiva());
        if (zonaRiesgoId != null) {
            alerta.setZonaRiesgo(buscarZona(zonaRiesgoId));
        }
        return alertaRepository.save(alerta);
    }

    public void eliminar(Long id) {
        if (!alertaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Alerta no encontrada con id " + id);
        }
        alertaRepository.deleteById(id);
    }

    private ZonaRiesgo buscarZona(Long id) {
        return zonaRiesgoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona de riesgo no encontrada con id " + id));
    }
}
