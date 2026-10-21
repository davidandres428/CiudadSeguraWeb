package com.ciudadsegura.config;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = CiudadSeguraApplication.class,
        properties = {
                "app.seed.enabled=true",
                "app.seed.admin-password=local-seed-test-password",
                "spring.datasource.url=jdbc:h2:mem:data_initializer_test;DB_CLOSE_DELAY=-1"
        })
@AutoConfigureMockMvc
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Autowired
    private ReporteIncidenteRepository reporteRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void inicializaDatosGeograficosVariadosYNoDuplicaNiPueblaUnaBaseParcial() {
        assertEquals(4, usuarioRepository.count());
        assertEquals(4, zonaRiesgoRepository.count());
        assertEquals(6, reporteRepository.count());
        assertEquals(4, alertaRepository.count());

        Usuario admin = usuarioRepository.findByEmail("admin.demo@ciudadsegura.local").orElseThrow();
        assertEquals(RolUsuario.ADMINISTRADOR, admin.getRol());
        assertTrue(passwordEncoder.matches("local-seed-test-password", admin.getPassword()));
        assertEquals(3, usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRol() == RolUsuario.CIUDADANO)
                .count());

        var zonas = zonaRiesgoRepository.findAll();
        assertEquals(EnumSet.allOf(NivelRiesgo.class),
                zonas.stream().map(zona -> zona.getNivelRiesgo())
                        .collect(java.util.stream.Collectors.toSet()));
        zonas.forEach(zona -> {
            assertTrue(zona.getLatitud() >= -90 && zona.getLatitud() <= 90);
            assertTrue(zona.getLongitud() >= -180 && zona.getLongitud() <= 180);
        });

        var reportes = reporteRepository.findAll();
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getLatitud() != null
                && reporte.getLongitud() != null
                && reporte.getLatitud() >= -90 && reporte.getLatitud() <= 90
                && reporte.getLongitud() >= -180 && reporte.getLongitud() <= 180));
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getUsuario() != null));
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getFechaReporte()
                .isAfter(LocalDateTime.now().minusDays(7))));
        assertTrue(reportes.stream().map(reporte -> reporte.getTipo()).collect(
                java.util.stream.Collectors.toSet()).containsAll(
                EnumSet.of(TipoReporte.ROBO, TipoReporte.VANDALISMO,
                        TipoReporte.ILUMINACION_DEFICIENTE, TipoReporte.SOSPECHA)));
        assertTrue(reportes.stream().map(reporte -> reporte.getEstado()).collect(
                java.util.stream.Collectors.toSet()).containsAll(
                EnumSet.of(EstadoReporte.PENDIENTE, EstadoReporte.EN_REVISION, EstadoReporte.RESUELTO)));

        var alertas = alertaRepository.findAll();
        assertEquals(3, alertas.stream().filter(alerta -> alerta.isActiva()).count());
        assertEquals(1, alertas.stream().filter(alerta -> !alerta.isActiva()).count());
        assertTrue(alertas.stream().allMatch(alerta -> alerta.getZonaRiesgo() != null
                && alerta.getFechaCreacion().isAfter(LocalDateTime.now().minusDays(7))));

        dataInitializer.run();
        assertEquals(4, usuarioRepository.count());
        assertEquals(4, zonaRiesgoRepository.count());
        assertEquals(6, reporteRepository.count());
        assertEquals(4, alertaRepository.count());

        alertaRepository.deleteAll();
        reporteRepository.deleteAll();
        zonaRiesgoRepository.deleteAll();
        usuarioRepository.deleteAll();
        Usuario existente = new Usuario("Usuario existente", "existente@example.test",
                passwordEncoder.encode("una-clave-local"));
        existente.setRol(RolUsuario.CIUDADANO);
        usuarioRepository.save(existente);

        dataInitializer.run();

        assertEquals(1, usuarioRepository.count());
        assertEquals(0, zonaRiesgoRepository.count());
        assertEquals(0, reporteRepository.count());
        assertEquals(0, alertaRepository.count());
        assertNotNull(usuarioRepository.findByEmail("existente@example.test").orElse(null));
        assertFalse(passwordEncoder.matches("local-seed-test-password", existente.getPassword()));
    }
}
