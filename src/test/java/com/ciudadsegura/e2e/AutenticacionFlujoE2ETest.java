package com.ciudadsegura.e2e;

import com.ciudadsegura.CiudadSeguraApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
@Transactional
class AutenticacionFlujoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registroLoginYCreacionDeReporte_persisteLaCuentaCiudadanaYEmiteJwt() throws Exception {
        String email = "ciudadano-e2e@example.com";
        String password = "clave-segura-123";
        String registro = """
                {"nombre":"Ciudadano E2E","email":"%s","password":"%s","rol":"ADMINISTRADOR"}
                """.formatted(email, password);

        String registroBody = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CIUDADANO"))
                .andReturn().getResponse().getContentAsString();
        assertFalse(extraerCampo(registroBody, "accessToken").isBlank());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("CIUDADANO"))
                .andReturn();
        String authBody = loginResult.getResponse().getContentAsString();
        String token = extraerCampo(authBody, "accessToken");
        long usuarioId = Long.parseLong(extraerCampo(authBody, "id"));

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Reporte E2E","descripcion":"Prueba de persistencia",
                                "tipo":"ROBO","prioridad":"ALTA","direccion":"Calle E2E 10"}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult reporteResult = mockMvc.perform(post("/api/reportes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Reporte E2E","descripcion":"Prueba de persistencia",
                                "tipo":"ROBO","prioridad":"ALTA","direccion":"Calle E2E 10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id").value(usuarioId))
                .andReturn();
        long reporteId = Long.parseLong(extraerCampo(reporteResult.getResponse().getContentAsString(), "id"));

        mockMvc.perform(get("/api/reportes/{id}", reporteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Reporte E2E"))
                .andExpect(jsonPath("$.usuario.id").value(usuarioId));
        MvcResult countResult = mockMvc.perform(get("/api/reportes/count"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(1, Long.parseLong(countResult.getResponse().getContentAsString()));
    }

    private String extraerCampo(String json, String nombre) {
        Matcher matcher = Pattern.compile("\"" + nombre + "\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+))").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("No se encontró el campo JSON: " + nombre);
        }
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }
}
