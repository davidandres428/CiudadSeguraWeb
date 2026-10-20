package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteIncidenteService {

    private final ReporteIncidenteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteIncidenteService(ReporteIncidenteRepository reporteRepository, UsuarioRepository usuarioRepository) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ReporteIncidente> listarTodos() {
        return reporteRepository.findAll();
    }

    public List<ReporteIncidente> listarPorUsuario(Long usuarioId) {
        return reporteRepository.findByUsuarioId(usuarioId);
    }

    public List<ReporteIncidente> listarPorEstado(EstadoReporte estado) {
        return reporteRepository.findByEstado(estado);
    }

    public ReporteIncidente buscarPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado con id " + id));
    }

    public ReporteIncidente crear(ReporteIncidente reporte, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + usuarioId));
        reporte.setUsuario(usuario);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        return reporteRepository.save(reporte);
    }

    public ReporteIncidente actualizar(Long id, ReporteIncidente datos) {
        ReporteIncidente reporte = buscarPorId(id);
        reporte.setTitulo(datos.getTitulo());
        reporte.setDescripcion(datos.getDescripcion());
        reporte.setTipo(datos.getTipo());
        reporte.setPrioridad(datos.getPrioridad());
        reporte.setDireccion(datos.getDireccion());
        reporte.setLatitud(datos.getLatitud());
        reporte.setLongitud(datos.getLongitud());
        if (datos.getEstado() != null) {
            reporte.setEstado(datos.getEstado());
        }
        return reporteRepository.save(reporte);
    }

    public void eliminar(Long id) {
        if (!reporteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Reporte no encontrado con id " + id);
        }
        reporteRepository.deleteById(id);
    }
}
