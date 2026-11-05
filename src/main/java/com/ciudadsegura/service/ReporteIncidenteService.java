package com.ciudadsegura.service;

import com.ciudadsegura.dto.ReporteMetricasResponse;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReporteIncidenteService {

    private final ReporteIncidenteRepository reporteIncidenteRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteIncidenteService(
            ReporteIncidenteRepository reporteIncidenteRepository,
            UsuarioRepository usuarioRepository) {
        this.reporteIncidenteRepository = reporteIncidenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ReporteIncidente> listarFiltrados(
            Long usuarioId, EstadoReporte estado, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        return reporteIncidenteRepository.findAll((root, query, builder) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (usuarioId != null) {
                filtros.add(builder.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (estado != null) {
                filtros.add(builder.equal(root.get("estado"), estado));
            }
            if (inicio != null) {
                filtros.add(builder.greaterThanOrEqualTo(root.get("fechaReporte"), inicio));
            }
            if (fin != null) {
                filtros.add(builder.lessThan(root.get("fechaReporte"), fin));
            }
            return builder.and(filtros.toArray(Predicate[]::new));
        });
    }

    @Transactional(readOnly = true)
    public ReporteMetricasResponse obtenerMetricas(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        Map<String, Long> porEstado = conteos(EstadoReporte.values(),
                reporteIncidenteRepository.contarPorEstado(inicio, fin));
        Map<String, Long> porPrioridad = conteos(PrioridadReporte.values(),
                reporteIncidenteRepository.contarPorPrioridad(inicio, fin));
        Map<String, Long> porTipo = conteos(TipoReporte.values(),
                reporteIncidenteRepository.contarPorTipo(inicio, fin));
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();

        return new ReporteMetricasResponse(total, porEstado, porPrioridad, porTipo);
    }

    @Transactional(readOnly = true)
    public String exportarCsv(List<ReporteIncidente> reportes) {
        StringBuilder csv = new StringBuilder("\uFEFFid,titulo,descripcion,tipo,prioridad,estado,direccion,latitud,longitud,fecha\n");
        for (ReporteIncidente reporte : reportes) {
            csv.append(reporte.getId()).append(',')
                    .append(celdaCsv(reporte.getTitulo())).append(',')
                    .append(celdaCsv(reporte.getDescripcion())).append(',')
                    .append(celdaCsv(reporte.getTipo())).append(',')
                    .append(celdaCsv(reporte.getPrioridad())).append(',')
                    .append(celdaCsv(reporte.getEstado())).append(',')
                    .append(celdaCsv(reporte.getDireccion())).append(',')
                    .append(celdaCsv(reporte.getLatitud())).append(',')
                    .append(celdaCsv(reporte.getLongitud())).append(',')
                    .append(celdaCsv(reporte.getFechaReporte())).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public ReporteIncidente buscarPorId(Long id) {
        return reporteIncidenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado con id " + id));
    }

    @Transactional
    public ReporteIncidente crear(ReporteIncidente datos, String emailUsuario) {
        Usuario usuario = buscarUsuarioAutenticado(emailUsuario);

        ReporteIncidente reporte = new ReporteIncidente();
        copiarDatosEditables(datos, reporte);
        reporte.setUsuario(usuario);
        return reporteIncidenteRepository.save(reporte);
    }

    private Usuario buscarUsuarioAutenticado(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new CredencialesInvalidasException(
                    "No se pudo identificar al usuario autenticado para asociar el registro.");
        }
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario registrado para asociar el registro."));
    }

    @Transactional
    public ReporteIncidente actualizar(
            Long id, ReporteIncidente datos, String emailUsuario, boolean esAdministrador) {
        ReporteIncidente reporte = buscarPorId(id);
        verificarPermiso(reporte, emailUsuario, esAdministrador);
        copiarDatosEditables(datos, reporte);
        if (esAdministrador && datos.getEstado() != null) {
            reporte.setEstado(datos.getEstado());
        }
        return reporteIncidenteRepository.save(reporte);
    }

    @Transactional
    public void eliminar(Long id, String emailUsuario, boolean esAdministrador) {
        ReporteIncidente reporte = buscarPorId(id);
        verificarPermiso(reporte, emailUsuario, esAdministrador);
        reporteIncidenteRepository.delete(reporte);
    }

    private void copiarDatosEditables(ReporteIncidente origen, ReporteIncidente destino) {
        destino.setTitulo(origen.getTitulo());
        destino.setDescripcion(origen.getDescripcion());
        destino.setTipo(origen.getTipo());
        destino.setPrioridad(origen.getPrioridad());
        destino.setDireccion(origen.getDireccion());
        destino.setLatitud(origen.getLatitud());
        destino.setLongitud(origen.getLongitud());
    }

    private void verificarPermiso(ReporteIncidente reporte, String emailUsuario, boolean esAdministrador) {
        if (!esAdministrador && (emailUsuario == null || reporte.getUsuario() == null
                || !reporte.getUsuario().getEmail().equalsIgnoreCase(emailUsuario))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el propietario o un administrador puede modificar este reporte");
        }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private <E extends Enum<E>> Map<String, Long> conteos(E[] valores, List<Object[]> resultados) {
        Map<String, Long> conteos = new java.util.LinkedHashMap<>();
        for (E valor : valores) {
            conteos.put(valor.name(), 0L);
        }
        for (Object[] resultado : resultados) {
            conteos.put(String.valueOf(resultado[0]), ((Number) resultado[1]).longValue());
        }
        return conteos;
    }

    private String celdaCsv(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        String comprobacion = texto.stripLeading();
        if (!comprobacion.isEmpty() && "=+-@".indexOf(comprobacion.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
