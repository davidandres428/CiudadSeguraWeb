package com.ciudadsegura.controller;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.SeveridadAlerta;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.LoteSemillaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:seedmasivo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.seed.enabled=false"
})
class SeedMasivoControllerTest {

    private static final String ENDPOINT = "/api/admin/seed-masivo";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Autowired
    private ReporteIncidenteRepository reporteRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private LoteSemillaRepository loteSemillaRepository;

    @Test
    void sembradoMasivo_sinToken_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(post(ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sembradoMasivo_conTokenCiudadano_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void sembradoMasivo_conTokenAdministrador_debeCrearElLoteSolicitado() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios").value(10))
                .andExpect(jsonPath("$.zonas").value(10))
                .andExpect(jsonPath("$.reportes").value(30))
                .andExpect(jsonPath("$.alertas").value(12));

        assertTrue(usuarioRepository.findAll().stream()
                .allMatch(usuario -> usuario.getRol() == RolUsuario.CIUDADANO));
        assertEquals(10, reporteRepository.findAll().stream()
                .map(reporte -> reporte.getUsuario().getId()).distinct().count());
        assertTrue(reporteRepository.findAll().stream()
                .anyMatch(reporte -> reporte.getTipo() == TipoReporte.VENTA_ESTUPEFACIENTES));
        assertTrue(reporteRepository.findAll().stream()
                .anyMatch(reporte -> reporte.getTipo() == TipoReporte.RUIDO_EXCESIVO));
        assertTrue(alertaRepository.findAll().stream()
                .allMatch(alerta -> alerta.getSeveridad() != null));
        assertTrue(Arrays.stream(SeveridadAlerta.values()).allMatch(severidad ->
                alertaRepository.findAll().stream().anyMatch(alerta -> alerta.getSeveridad() == severidad)));
        assertTrue(alertaRepository.findAll().stream().anyMatch(alerta -> alerta.isActiva()));
        assertTrue(alertaRepository.findAll().stream().anyMatch(alerta -> !alerta.isActiva()));
        assertTrue(zonaRiesgoRepository.findAll().stream()
                .map(zona -> zona.getNivelRiesgo()).distinct().count() >= 3);
        assertTrue(reporteRepository.findAll().stream().allMatch(reporte ->
                reporte.getLatitud() > 4.5 && reporte.getLatitud() < 4.9
                        && reporte.getLongitud() < -73.8 && reporte.getLongitud() > -74.3
                        && reporte.getFechaReporte().isAfter(LocalDateTime.now().minusDays(30))
                        && reporte.getDireccion().contains(" — ")));
    }

    @Test
    @Transactional
    void sembradoMasivo_segundaEjecucionDebeResponderConflictoSinDuplicar() throws Exception {
        String token = crearToken("ADMINISTRADOR");
        mockMvc.perform(post(ENDPOINT).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post(ENDPOINT).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El sembrado masivo ya fue ejecutado anteriormente."));

        assertEquals(10, usuarioRepository.count());
        assertEquals(10, zonaRiesgoRepository.count());
        assertEquals(30, reporteRepository.count());
        assertEquals(12, alertaRepository.count());
        assertEquals(1, loteSemillaRepository.count());
    }

    private String crearToken(String rol) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ciudad-segura")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(5, ChronoUnit.MINUTES))
                .subject("semilla@example.com")
                .claim("scope", rol)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
