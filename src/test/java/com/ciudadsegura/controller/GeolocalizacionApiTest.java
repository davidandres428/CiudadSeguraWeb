package com.ciudadsegura.controller;

import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.service.ReporteIncidenteService;
import com.ciudadsegura.service.ZonaRiesgoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GeolocalizacionApiTest {

    @Test
    void listarReportes_debeIncluirLatitudYLongitudEnElJson() throws Exception {
        ReporteIncidenteService servicio = mock(ReporteIncidenteService.class);
        ReporteIncidente reporte = new ReporteIncidente();
        reporte.setTitulo("Incidente de prueba");
        reporte.setDescripcion("Descripción de prueba");
        reporte.setTipo(TipoReporte.ROBO);
        reporte.setPrioridad(PrioridadReporte.ALTA);
        reporte.setDireccion("Calle de prueba");
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setLatitud(4.711);
        reporte.setLongitud(-74.0721);
        when(servicio.listarTodos()).thenReturn(List.of(reporte));
        MockMvc mvc = standaloneSetup(new ReporteIncidenteController(servicio)).build();

        mvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitud").value(4.711))
                .andExpect(jsonPath("$[0].longitud").value(-74.0721));
    }

    @Test
    void listarZonas_debeIncluirLatitudYLongitudEnElJson() throws Exception {
        ZonaRiesgoService servicio = mock(ZonaRiesgoService.class);
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre("Zona de prueba");
        zona.setNivelRiesgo(NivelRiesgo.ALTO);
        zona.setLatitud(4.598);
        zona.setLongitud(-74.076);
        when(servicio.listarTodas()).thenReturn(List.of(zona));
        MockMvc mvc = standaloneSetup(new ZonaRiesgoController(servicio)).build();

        mvc.perform(get("/api/zonas-riesgo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitud").value(4.598))
                .andExpect(jsonPath("$[0].longitud").value(-74.076));
    }
}
