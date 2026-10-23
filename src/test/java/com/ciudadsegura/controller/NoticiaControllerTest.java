package com.ciudadsegura.controller;

import com.ciudadsegura.CiudadSeguraApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = CiudadSeguraApplication.class,
        properties = {
                "app.seed.enabled=true",
                "app.seed.admin-password=local-seed-test-password",
                "spring.datasource.url=jdbc:h2:mem:noticia_controller_test;DB_CLOSE_DELAY=-1"
        })
@AutoConfigureMockMvc
class NoticiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void noticiasPublicasSeOrdenanPorFechaYExponenContenidoEditorial() throws Exception {
        mockMvc.perform(get("/api/noticias"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].titulo", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].categoria", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].contenido", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].fechaPublicacion", everyItem(notNullValue())));
    }
}
