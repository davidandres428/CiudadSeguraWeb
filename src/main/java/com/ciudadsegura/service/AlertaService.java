package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final UsuarioRepository usuarioRepository;

    public AlertaService(
            AlertaRepository alertaRepository,
            ZonaRiesgoRepository zonaRiesgoRepository,
            UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Alerta> listarTodas() {
        return alertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long contar() {
        return alertaRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return alertaRepository.countByActivaTrue();
    }

    @Transactional(readOnly = true)
    public List<Alerta> listarActivas() {
        return alertaRepository.findByActivaTrue();
    }

    @Transactional(readOnly = true)
    public Alerta buscarPorId(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada con id " + id));
    }

    @Transactional
    public Alerta crear(Alerta datos, Long zonaRiesgoId, String emailUsuario) {
        Usuario creador = buscarUsuarioAutenticado(emailUsuario);
        Alerta alerta = new Alerta();
        alerta.setTitulo(datos.getTitulo());
        alerta.setMensaje(datos.getMensaje());
        alerta.setTipo(datos.getTipo());
        alerta.setSeveridad(datos.getSeveridad());
        alerta.setActiva(datos.isActiva());
        if (zonaRiesgoId != null) {
            alerta.setZonaRiesgo(buscarZona(zonaRiesgoId));
        }
        alerta.setCreador(creador);
        return alertaRepository.save(alerta);
    }

    @Transactional
    public Alerta actualizar(Long id, Alerta datos, Long zonaRiesgoId) {
        Alerta alerta = buscarPorId(id);
        alerta.setTitulo(datos.getTitulo());
        alerta.setMensaje(datos.getMensaje());
        alerta.setTipo(datos.getTipo());
        alerta.setSeveridad(datos.getSeveridad());
        alerta.setActiva(datos.isActiva());
        if (zonaRiesgoId != null) {
            alerta.setZonaRiesgo(buscarZona(zonaRiesgoId));
        }
        return alertaRepository.save(alerta);
    }

    @Transactional
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

    private Usuario buscarUsuarioAutenticado(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new CredencialesInvalidasException(
                    "No se pudo identificar al usuario autenticado para asociar la alerta.");
        }
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario registrado para asociar la alerta."));
    }
}
