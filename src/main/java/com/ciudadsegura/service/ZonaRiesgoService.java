package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ZonaRiesgoService {

    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final UsuarioRepository usuarioRepository;

    public ZonaRiesgoService(
            ZonaRiesgoRepository zonaRiesgoRepository,
            UsuarioRepository usuarioRepository) {
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ZonaRiesgo> listarTodas() {
        return zonaRiesgoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long contar() {
        return zonaRiesgoRepository.count();
    }

    @Transactional(readOnly = true)
    public List<ZonaRiesgo> listarPorNivel(NivelRiesgo nivel) {
        return zonaRiesgoRepository.findByNivelRiesgo(nivel);
    }

    @Transactional(readOnly = true)
    public ZonaRiesgo buscarPorId(Long id) {
        return zonaRiesgoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona de riesgo no encontrada con id " + id));
    }

    @Transactional
    public ZonaRiesgo crear(ZonaRiesgo datos, String emailUsuario) {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre(datos.getNombre());
        zona.setDescripcion(datos.getDescripcion());
        zona.setNivelRiesgo(datos.getNivelRiesgo());
        zona.setLatitud(datos.getLatitud());
        zona.setLongitud(datos.getLongitud());
        zona.setEstadoCamaras(datos.getEstadoCamaras());
        zona.setUltimaInspeccion(datos.getUltimaInspeccion());
        zona.setCreador(buscarUsuarioAutenticado(emailUsuario));
        return zonaRiesgoRepository.save(zona);
    }

    @Transactional
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

    @Transactional
    public void eliminar(Long id) {
        if (!zonaRiesgoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Zona de riesgo no encontrada con id " + id);
        }
        zonaRiesgoRepository.deleteById(id);
    }

    private Usuario buscarUsuarioAutenticado(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new CredencialesInvalidasException(
                    "No se pudo identificar al usuario autenticado para asociar la zona de riesgo.");
        }
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario registrado para asociar la zona de riesgo."));
    }
}
