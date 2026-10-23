package com.ciudadsegura.config;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.Noticia;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.NoticiaRepository;
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
    private NoticiaRepository noticiaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void inicializaDatosGeograficosVariadosYNoDuplicaNiPueblaUnaBaseParcial() {
        assertEquals(4, usuarioRepository.count());
        assertEquals(15, zonaRiesgoRepository.count());
        assertEquals(128, reporteRepository.count());
        assertEquals(3, alertaRepository.count());
        assertEquals(5, noticiaRepository.count());

        Usuario admin = usuarioRepository.findByEmail("david.andresgalvis@gmail.com").orElseThrow();
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
            assertTrue(zona.getEstadoCamaras() != null && !zona.getEstadoCamaras().isBlank());
            assertNotNull(zona.getUltimaInspeccion());
        });

        var reportes = reporteRepository.findAll();
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getLatitud() != null
                && reporte.getLongitud() != null
                && reporte.getLatitud() >= -90 && reporte.getLatitud() <= 90
                && reporte.getLongitud() >= -180 && reporte.getLongitud() <= 180));
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getUsuario() != null));
        assertTrue(reportes.stream().allMatch(reporte -> reporte.getFechaReporte()
                .isAfter(LocalDateTime.now().minusDays(31))));
        assertTrue(reportes.stream().map(reporte -> reporte.getTipo()).distinct().count() >= 4);
        assertTrue(reportes.stream().map(reporte -> reporte.getEstado()).distinct().count() >= 3);
        assertTrue(reportes.stream().map(reporte -> reporte.getTipo()).collect(
                java.util.stream.Collectors.toSet()).containsAll(
                EnumSet.of(TipoReporte.ROBO, TipoReporte.VANDALISMO,
                        TipoReporte.ILUMINACION_DEFICIENTE, TipoReporte.SOSPECHA)));
        assertTrue(reportes.stream().map(reporte -> reporte.getEstado()).collect(
                java.util.stream.Collectors.toSet()).containsAll(
                EnumSet.of(EstadoReporte.PENDIENTE, EstadoReporte.EN_REVISION, EstadoReporte.RESUELTO)));

        var alertas = alertaRepository.findAll();
        assertEquals(3, alertas.stream().filter(alerta -> alerta.isActiva()).count());
        assertTrue(alertas.stream().allMatch(alerta -> alerta.getZonaRiesgo() != null
                && alerta.getFechaCreacion().isAfter(LocalDateTime.now().minusDays(7))));

        var noticias = noticiaRepository.findAll();
        assertTrue(noticias.stream().allMatch(noticia -> !noticia.getTitulo().isBlank()
                && !noticia.getContenido().isBlank()
                && !noticia.getCategoria().isBlank()
                && noticia.getFechaPublicacion().isAfter(LocalDateTime.now().minusDays(31))));

        dataInitializer.run();
        assertEquals(4, usuarioRepository.count());
        assertEquals(15, zonaRiesgoRepository.count());
        assertEquals(128, reporteRepository.count());
        assertEquals(3, alertaRepository.count());
        assertEquals(5, noticiaRepository.count());

        noticiaRepository.deleteAll();
        alertaRepository.deleteAll();
        reporteRepository.deleteAll();
        zonaRiesgoRepository.deleteAll();
        usuarioRepository.deleteAll();
        Usuario existente = new Usuario("Usuario existente", "existente@example.test",
                passwordEncoder.encode("una-clave-local"));
        existente.setRol(RolUsuario.CIUDADANO);
        usuarioRepository.save(existente);

        dataInitializer.run();

        assertEquals(2, usuarioRepository.count());
        assertEquals(0, zonaRiesgoRepository.count());
        assertEquals(0, reporteRepository.count());
        assertEquals(0, alertaRepository.count());
        assertEquals(0, noticiaRepository.count());
        assertNotNull(usuarioRepository.findByEmail("existente@example.test").orElse(null));
        assertEquals(RolUsuario.ADMINISTRADOR, usuarioRepository
                .findByEmail("david.andresgalvis@gmail.com").orElseThrow().getRol());
        assertFalse(passwordEncoder.matches("local-seed-test-password", existente.getPassword()));
    }
}
