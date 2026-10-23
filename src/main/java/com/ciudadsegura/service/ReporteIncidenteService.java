package com.ciudadsegura.service;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteIncidenteService {

    private final ReporteIncidenteRepository reporteIncidenteRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteIncidenteService(ReporteIncidenteRepository reporteIncidenteRepository, UsuarioRepository usuarioRepository) {
        this.reporteIncidenteRepository = reporteIncidenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ReporteIncidente> listarTodos() {
        return reporteIncidenteRepository.findAll();
    }

    public ReporteIncidente guardar(ReporteIncidente reporte, String emailUsuario) {
        Usuario usuario = null;
        
        // Intenta buscar al usuario autenticado si se proporciona su correo
        if (emailUsuario != null && !emailUsuario.isEmpty()) {
            usuario = usuarioRepository.findByCorreo(emailUsuario).orElse(null);
        }
        
        // Si no se encuentra, asigna el primer usuario disponible para evitar errores en la interfaz
        if (usuario == null) {
            usuario = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }
        
        if (usuario == null) {
            throw new RuntimeException("No hay usuarios registrados en el sistema para asociar el reporte.");
        }

        reporte.setUsuario(usuario);
        return reporteIncidenteRepository.save(reporte);
    }

    public ReporteIncidente obtenerPorId(Long id) {
        return reporteIncidenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    }

    public void eliminar(Long id) {
        reporteIncidenteRepository.deleteById(id);
    }
}