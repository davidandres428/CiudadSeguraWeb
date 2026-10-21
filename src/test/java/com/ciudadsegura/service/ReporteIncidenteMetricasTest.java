package com.ciudadsegura.service;

import com.ciudadsegura.dto.ReporteMetricasResponse;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteIncidenteMetricasTest {

    @Mock
    private ReporteIncidenteRepository reporteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReporteIncidenteService reporteService;

    @Test
    void obtenerMetricas_debeCompletarEstadosYSumarTotales() {
        when(reporteRepository.contarPorEstado(null, null))
                .thenReturn(List.<Object[]>of(new Object[]{EstadoReporte.PENDIENTE, 2L}));
        when(reporteRepository.contarPorPrioridad(null, null))
                .thenReturn(List.<Object[]>of(new Object[]{PrioridadReporte.ALTA, 2L}));
        when(reporteRepository.contarPorTipo(null, null))
                .thenReturn(List.<Object[]>of(new Object[]{TipoReporte.ROBO, 2L}));

        ReporteMetricasResponse resultado = reporteService.obtenerMetricas(null, null);

        assertEquals(2, resultado.getTotal());
        assertEquals(2, resultado.getPorEstado().get("PENDIENTE"));
        assertEquals(0, resultado.getPorEstado().get("RESUELTO"));
        assertEquals(2, resultado.getPorPrioridad().get("ALTA"));
        assertEquals(2, resultado.getPorTipo().get("ROBO"));
    }

    @Test
    void exportarCsv_debeEscaparCamposYProtegerFormulas() {
        ReporteIncidente reporte = new ReporteIncidente();
        reporte.setId(7L);
        reporte.setTitulo("=HYPERLINK(\"https://example.com\")");
        reporte.setDescripcion("Descripción, con coma");
        reporte.setTipo(TipoReporte.ROBO);
        reporte.setPrioridad(PrioridadReporte.ALTA);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setDireccion("Calle \"Principal\"");

        String csv = reporteService.exportarCsv(List.of(reporte));

        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"https://example.com\"\")\""));
        assertTrue(csv.contains("\"Descripción, con coma\""));
        assertTrue(csv.contains("\"Calle \"\"Principal\"\"\""));
    }

    @Test
    void obtenerMetricas_debeAceptarRangoDeFechasAscendente() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);
        when(reporteRepository.contarPorEstado(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of());
        when(reporteRepository.contarPorPrioridad(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of());
        when(reporteRepository.contarPorTipo(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of());

        ReporteMetricasResponse resultado = reporteService.obtenerMetricas(desde, hasta);

        assertEquals(0, resultado.getTotal());
    }

    @Test
    void obtenerMetricas_debeRechazarRangoDeFechasInvertido() {
        assertThrows(IllegalArgumentException.class, () -> reporteService.obtenerMetricas(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 31)));
    }
}
