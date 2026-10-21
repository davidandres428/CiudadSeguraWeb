package com.ciudadsegura.controller;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.repository.RadicacionPqrsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
@Transactional
class PqrsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RadicacionPqrsRepository radicacionRepository;

    @Test
    void radicar_sinSesionGuardaLaSolicitudYRetornaConfirmacion() throws Exception {
        mockMvc.perform(post("/api/pqrs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ciudadana de prueba","email":"pqrs@example.com",
                                "tipo":"PETICION","asunto":"Iluminación en el barrio",
                                "detalle":"Solicito revisar las luminarias del sector."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("RADICADA"))
                .andExpect(jsonPath("$.fechaRadicacion").isNotEmpty());

        var radicacion = radicacionRepository.findAll().stream()
                .filter(record -> "pqrs@example.com".equals(record.getEmail()))
                .findFirst()
                .orElseThrow();
        assertEquals("pqrs@example.com", radicacion.getEmail());
        assertEquals("Iluminación en el barrio", radicacion.getAsunto());
        assertNotNull(radicacion.getFechaRadicacion());
    }

    @Test
    void radicar_debeValidarElCorreo() throws Exception {
        mockMvc.perform(post("/api/pqrs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ciudadana de prueba","email":"correo-invalido",
                                "tipo":"PETICION","asunto":"Solicitud",
                                "detalle":"Detalle de la solicitud."}
                                """))
                .andExpect(status().isBadRequest());
    }
}
