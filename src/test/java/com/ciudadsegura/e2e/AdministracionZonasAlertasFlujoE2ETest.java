package com.ciudadsegura.e2e;

import com.ciudadsegura.CiudadSeguraApplication;
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

    @Test
    void mutacionesDeZonasYAlertasExigenAdminYActualizanConteosPublicos() throws Exception {
        String ciudadano = crearToken("ciudadano@example.com", "CIUDADANO");
        String administrador = crearToken("admin@example.com", "ADMINISTRADOR");

        mockMvc.perform(get("/api/zonas-riesgo/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
        mockMvc.perform(get("/api/alertas/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
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
                                {"nombre":"Zona denegada","nivelRiesgo":"ALTO","latitud":4.6,"longitud":-74.1}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/alertas")
                        .header("Authorization", "Bearer " + ciudadano)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alerta denegada","mensaje":"Sin permisos","tipo":"SEGURIDAD","activa":true}
                                """))
                .andExpect(status().isForbidden());

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
                .andExpect(jsonPath("$[0].nombre").value("Zona E2E"));
        mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Alerta E2E"));
        mockMvc.perform(get("/api/zonas-riesgo/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
        mockMvc.perform(get("/api/alertas/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
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
}
