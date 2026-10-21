package com.ciudadsegura.e2e;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
@Transactional
class ReportesFlujoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void reporteSoloSeCreaConJwtYElConteoReflejaLaPersistencia() throws Exception {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Reporte E2E", "reportes-e2e@example.com", "hash-no-usado"));
        String token = crearToken(usuario.getEmail(), RolUsuario.CIUDADANO);
        String reporte = """
                {"titulo":"Incidente de prueba","descripcion":"Validación API end-to-end",
                "tipo":"VANDALISMO","prioridad":"MEDIA","direccion":"Carrera 20"}
                """;

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reporte))
                .andExpect(status().isUnauthorized());

        MvcResult creado = mockMvc.perform(post("/api/reportes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reporte))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Incidente de prueba"))
                .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
                .andReturn();
        long id = Long.parseLong(extraerId(creado.getResponse().getContentAsString()));

        mockMvc.perform(get("/api/reportes").param("usuarioId", usuario.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mockMvc.perform(get("/api/reportes/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    private String crearToken(String email, RolUsuario rol) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ciudad-segura")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(30, ChronoUnit.MINUTES))
                .subject(email)
                .claim("scope", rol.name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String extraerId(String json) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("No se encontró el id del reporte");
        }
        return matcher.group(1);
    }
}
