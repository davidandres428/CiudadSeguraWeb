package com.ciudadsegura.service;

import com.ciudadsegura.dto.ReporteMetricasResponse;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<ReporteIncidente> listarFiltrados(
            Long usuarioId,
            EstadoReporte estado,
            LocalDate desde,
            LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        LocalDateTime desdeFecha = inicio(desde);
        LocalDateTime hastaFecha = finExclusivo(hasta);
        Specification<ReporteIncidente> filtros = (reporte, consulta, builder) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (usuarioId != null) {
                predicados.add(builder.equal(reporte.get("usuario").get("id"), usuarioId));
            }
            if (estado != null) {
                predicados.add(builder.equal(reporte.get("estado"), estado));
            }
            if (desdeFecha != null) {
                predicados.add(builder.greaterThanOrEqualTo(reporte.get("fechaReporte"), desdeFecha));
            }
            if (hastaFecha != null) {
                predicados.add(builder.lessThan(reporte.get("fechaReporte"), hastaFecha));
            }
            return builder.and(predicados.toArray(Predicate[]::new));
        };
        return reporteRepository.findAll(filtros, Sort.by(Sort.Direction.DESC, "fechaReporte"));
    }

    public ReporteMetricasResponse obtenerMetricas(LocalDate desde, LocalDate hasta) {
        validarRangoFechas(desde, hasta);
        LocalDateTime desdeFecha = inicio(desde);
        LocalDateTime hastaFecha = finExclusivo(hasta);
        Map<String, Long> porEstado = conteos(EstadoReporte.class,
                reporteRepository.contarPorEstado(desdeFecha, hastaFecha));
        Map<String, Long> porPrioridad = conteos(PrioridadReporte.class,
                reporteRepository.contarPorPrioridad(desdeFecha, hastaFecha));
        Map<String, Long> porTipo = conteos(TipoReporte.class,
                reporteRepository.contarPorTipo(desdeFecha, hastaFecha));
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        return new ReporteMetricasResponse(total, porEstado, porPrioridad, porTipo);
    }

    public String exportarCsv(List<ReporteIncidente> reportes) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        agregarFila(csv, "ID", "Título", "Descripción", "Tipo", "Prioridad", "Estado",
                "Dirección", "Latitud", "Longitud", "Fecha de reporte");
        for (ReporteIncidente reporte : reportes) {
            agregarFila(csv,
                    String.valueOf(reporte.getId()),
                    reporte.getTitulo(),
                    reporte.getDescripcion(),
                    reporte.getTipo().name(),
                    reporte.getPrioridad().name(),
                    reporte.getEstado().name(),
                    reporte.getDireccion(),
                    reporte.getLatitud() == null ? "" : reporte.getLatitud().toString(),
                    reporte.getLongitud() == null ? "" : reporte.getLongitud().toString(),
                    reporte.getFechaReporte() == null ? "" : reporte.getFechaReporte().toString());
        }
        return csv.toString();
    }

    private <E extends Enum<E>> Map<String, Long> conteos(Class<E> tipo, List<Object[]> resultados) {
        Map<String, Long> conteos = new LinkedHashMap<>();
        for (E valor : tipo.getEnumConstants()) {
            conteos.put(valor.name(), 0L);
        }
        for (Object[] resultado : resultados) {
            conteos.put(((Enum<?>) resultado[0]).name(), ((Number) resultado[1]).longValue());
        }
        return conteos;
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    private LocalDateTime inicio(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay();
    }

    private LocalDateTime finExclusivo(LocalDate fecha) {
        return fecha == null ? null : fecha.plusDays(1).atStartOfDay();
    }

    private void agregarFila(StringBuilder csv, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            String valor = valores[i] == null ? "" : valores[i];
            if (necesitaPrefijoSeguro(valor)) {
                valor = "'" + valor;
            }
            csv.append('"').append(valor.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }

    private boolean necesitaPrefijoSeguro(String valor) {
        String sinEspaciosIniciales = valor.stripLeading();
        return !sinEspaciosIniciales.isEmpty()
                && "=+-@\t\r".indexOf(sinEspaciosIniciales.charAt(0)) >= 0;
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
