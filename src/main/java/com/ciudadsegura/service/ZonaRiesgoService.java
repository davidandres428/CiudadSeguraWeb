package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZonaRiesgoService {

    private final ZonaRiesgoRepository zonaRiesgoRepository;

    public ZonaRiesgoService(ZonaRiesgoRepository zonaRiesgoRepository) {
        this.zonaRiesgoRepository = zonaRiesgoRepository;
    }

    public List<ZonaRiesgo> listarTodas() {
        return zonaRiesgoRepository.findAll();
    }

    public long contar() {
        return zonaRiesgoRepository.count();
    }

    public List<ZonaRiesgo> listarPorNivel(NivelRiesgo nivel) {
        return zonaRiesgoRepository.findByNivelRiesgo(nivel);
    }

    public ZonaRiesgo buscarPorId(Long id) {
        return zonaRiesgoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona de riesgo no encontrada con id " + id));
    }

    public ZonaRiesgo crear(ZonaRiesgo zona) {
        return zonaRiesgoRepository.save(zona);
    }

    public ZonaRiesgo actualizar(Long id, ZonaRiesgo datos) {
        ZonaRiesgo zona = buscarPorId(id);
        zona.setNombre(datos.getNombre());
        zona.setDescripcion(datos.getDescripcion());
        zona.setNivelRiesgo(datos.getNivelRiesgo());
        zona.setLatitud(datos.getLatitud());
        zona.setLongitud(datos.getLongitud());
        if (datos.getEstadoCamaras() != null) {
            zona.setEstadoCamaras(datos.getEstadoCamaras());
        }
        if (datos.getUltimaInspeccion() != null) {
            zona.setUltimaInspeccion(datos.getUltimaInspeccion());
        }
        return zonaRiesgoRepository.save(zona);
    }

    public void eliminar(Long id) {
        if (!zonaRiesgoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Zona de riesgo no encontrada con id " + id);
        }
        zonaRiesgoRepository.deleteById(id);
    }
}
