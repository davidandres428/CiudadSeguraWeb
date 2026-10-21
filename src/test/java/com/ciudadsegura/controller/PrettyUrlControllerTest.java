package com.ciudadsegura.controller;

import com.ciudadsegura.CiudadSeguraApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
class PrettyUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void raiz_sirveInicioSinRedireccionar() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void indexHtml_redirigeALaRaizYConservaLaConsulta() throws Exception {
        mockMvc.perform(get("/index.html").queryParam("origen", "enlace"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/?origen=enlace"));
    }

    @Test
    void vistasUsanRutasLimpiasYLasRutasHtmlRedirigen() throws Exception {
        mockMvc.perform(get("/reportes"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/reportes.html"));

        mockMvc.perform(get("/alertas.html").queryParam("filtro", "activas"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/alertas?filtro=activas"));

        mockMvc.perform(get("/alertas"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/alertas.html"));

        mockMvc.perform(get("/zonas"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/zonas.html"));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/usuarios.html"));

        mockMvc.perform(get("/usuarios.html"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/usuarios"));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/dashboard.html"));

        mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/dashboard"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login.html"));

        mockMvc.perform(get("/login.html"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/login"));

        mockMvc.perform(get("/zonas.html"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/zonas"));
    }

    @Test
    void rutasLimpiasConBarraFinal_redirigenALaFormaCanonica() throws Exception {
        mockMvc.perform(get("/reportes/").queryParam("filtro", "nuevo"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/reportes?filtro=nuevo"));

        mockMvc.perform(get("/login/"))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", "/login"));
    }

    @Test
    void rutasApiYRecursosEstaticosNoSeReescriben() throws Exception {
        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"));

        mockMvc.perform(get("/css/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }
}
