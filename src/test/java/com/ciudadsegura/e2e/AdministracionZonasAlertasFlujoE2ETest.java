package com.ciudadsegura.e2e;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
@Transactional
class AdministracionZonasAlertasFlujoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Test
    void ciudadanosCreanZonasYAdministradoresGestionanAlertas() throws Exception {
        String ciudadano = crearToken("ciudadano@example.com", "CIUDADANO");
        String administrador = crearToken("admin@example.com", "ADMINISTRADOR");
        guardarUsuario("ciudadano@example.com");
        guardarUsuario("admin@example.com");
        long zonasIniciales = zonaRiesgoRepository.count();
        long alertasIniciales = alertaRepository.count();

        mockMvc.perform(get("/api/zonas-riesgo/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(zonasIniciales));
        mockMvc.perform(get("/api/alertas/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(alertasIniciales));
        mockMvc.perform(post("/api/zonas-riesgo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Zona sin sesión","nivelRiesgo":"ALTO","latitud":4.6,"longitud":-74.1}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alerta sin sesión","mensaje":"Sin autenticación","tipo":"SEGURIDAD","activa":true}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/zonas-riesgo")
                        .header("Authorization", "Bearer " + ciudadano)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descripcion":"Faltan campos requeridos"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").exists());
        mockMvc.perform(post("/api/alertas")
                        .header("Authorization", "Bearer " + ciudadano)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mensaje":"Falta título","tipo":"SEGURIDAD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").exists());
        mockMvc.perform(post("/api/zonas-riesgo")
                        .header("Authorization", "Bearer " + ciudadano)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Zona denegada","nivelRiesgo":"ALTO","latitud":4.6,"longitud":-74.1}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/alertas")
                        .header("Authorization", "Bearer " + ciudadano)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alerta denegada","mensaje":"Sin permisos","tipo":"SEGURIDAD","activa":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Alerta denegada"));

        assertEquals("ciudadano@example.com", zonaRiesgoRepository.findAll().stream()
                .filter(zona -> "Zona denegada".equals(zona.getNombre()))
                .findFirst().orElseThrow().getCreador().getEmail());
        assertEquals("ciudadano@example.com", alertaRepository.findAll().stream()
                .filter(alerta -> "Alerta denegada".equals(alerta.getTitulo()))
                .findFirst().orElseThrow().getCreador().getEmail());

        mockMvc.perform(post("/api/zonas-riesgo")
                        .header("Authorization", "Bearer " + administrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Zona E2E","nivelRiesgo":"ALTO","latitud":4.6,"longitud":-74.1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Zona E2E"));
        mockMvc.perform(post("/api/alertas")
                        .header("Authorization", "Bearer " + administrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alerta E2E","mensaje":"Persistencia verificada","tipo":"SEGURIDAD","activa":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Alerta E2E"));

        mockMvc.perform(get("/api/zonas-riesgo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItems("Zona denegada", "Zona E2E")));
        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItems("Alerta denegada", "Alerta E2E")));
        mockMvc.perform(get("/api/zonas-riesgo/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(zonasIniciales + 2));
        mockMvc.perform(get("/api/alertas/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(alertasIniciales + 2));
    }

    @Test
    void preflightCorsPermiteOrigenesExternosSinCredencialesDeCookie() throws Exception {
        mockMvc.perform(options("/api/reportes")
                        .header("Origin", "https://frontend.example")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://frontend.example"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    private String crearToken(String email, String rol) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ciudad-segura")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(30, ChronoUnit.MINUTES))
                .subject(email)
                .claim("scope", rol)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private void guardarUsuario(String email) {
        usuarioRepository.save(new Usuario("Usuario de prueba", email, "clave-segura"));
    }
}
