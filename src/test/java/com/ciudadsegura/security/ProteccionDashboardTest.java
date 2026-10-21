package com.ciudadsegura.security;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(classes = CiudadSeguraApplication.class)
@AutoConfigureMockMvc
class ProteccionDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReporteIncidenteRepository reporteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void metricas_sinToken_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(get("/api/reportes/metricas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void metricas_conTokenCiudadano_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(get("/api/reportes/metricas")
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricas_conTokenAdministrador_debeRetornarConteos() throws Exception {
        mockMvc.perform(get("/api/reportes/metricas")
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.porEstado.PENDIENTE").value(0))
                .andExpect(jsonPath("$.porPrioridad.ALTA").value(0))
                .andExpect(jsonPath("$.porTipo.ROBO").value(0));
    }

    @Test
    void exportarCsv_conTokenCiudadano_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(get("/api/reportes/exportar/csv")
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void crudUsuarios_conTokenCiudadano_debeRechazarLaSolicitud() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crudUsuarios_conTokenAdministrador_debePermitirLaSolicitud() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void mutacionesDeZonasYAlertas_soloDebenPermitirAdministradores() throws Exception {
        String tokenCiudadano = crearToken("CIUDADANO");
        String tokenAdmin = crearToken("ADMINISTRADOR");

        mockMvc.perform(post("/api/zonas-riesgo").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/zonas-riesgo/1").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/zonas-riesgo/1").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/alertas").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/alertas/1").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/alertas/1").header("Authorization", "Bearer " + tokenCiudadano))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/zonas-riesgo")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Zona admin","nivelRiesgo":"ALTO","latitud":4.6,"longitud":-74.1}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/alertas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Alerta admin","mensaje":"Mensaje","tipo":"SEGURIDAD","activa":true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    void reporte_soloPuedeCrearloElPropietarioAutenticadoYNoPuedeSuplantarse() throws Exception {
        Usuario propietario = guardarUsuario("propietario@example.com", RolUsuario.CIUDADANO);
        Usuario otro = guardarUsuario("otro@example.com", RolUsuario.CIUDADANO);

        mockMvc.perform(post("/api/reportes")
                        .param("usuarioId", otro.getId().toString())
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO", propietario.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Reporte propio","descripcion":"Descripción del incidente",
                                "tipo":"ROBO","prioridad":"ALTA","direccion":"Calle 10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id").value(propietario.getId()))
                .andExpect(jsonPath("$.usuario.email").doesNotExist())
                .andExpect(jsonPath("$.usuario.nombre").doesNotExist());

        mockMvc.perform(post("/api/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Reporte anónimo","descripcion":"Descripción del incidente",
                                "tipo":"ROBO","prioridad":"ALTA","direccion":"Calle 10"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reporte_debeRechazarCoordenadasFueraDelRangoGeografico() throws Exception {
        Usuario propietario = guardarUsuario("coordenadas@example.com", RolUsuario.CIUDADANO);

        mockMvc.perform(post("/api/reportes")
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO", propietario.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Reporte inválido","descripcion":"Descripción del incidente",
                                "tipo":"ROBO","prioridad":"ALTA","direccion":"Calle 10",
                                "latitud":91,"longitud":-74}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void ciudadano_soloPuedeEditarYEliminarReportesPropiosYNoCambiarEstado() throws Exception {
        Usuario propietario = guardarUsuario("dueno@example.com", RolUsuario.CIUDADANO);
        Usuario otro = guardarUsuario("ajeno@example.com", RolUsuario.CIUDADANO);
        ReporteIncidente propio = guardarReporte(propietario, "Reporte propio");
        ReporteIncidente ajeno = guardarReporte(otro, "Reporte ajeno");
        String token = crearToken("CIUDADANO", propietario.getEmail());
        String cuerpo = """
                {"titulo":"Actualizado","descripcion":"Descripción actualizada",
                "tipo":"ROBO","prioridad":"MEDIA","estado":"RESUELTO","direccion":"Calle 12"}
                """;

        mockMvc.perform(put("/api/reportes/" + propio.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Actualizado"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        mockMvc.perform(put("/api/reportes/" + ajeno.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/reportes/" + ajeno.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/reportes/" + propio.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        ReporteIncidente reporteRestante = guardarReporte(otro, "Reporte para administrador");
        mockMvc.perform(delete("/api/reportes/" + reporteRestante.getId())
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    void ciudadano_noDebePoderAutoAsignarseRolAdministrador() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + crearToken("CIUDADANO"))
                        .content("""
                                {"nombre":"Ciudadano","email":"ciudadano@example.com","rol":"ADMINISTRADOR"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void login_deAdministrador_debeEmitirTokenAceptadoPorElDashboard() throws Exception {
        Usuario administrador = new Usuario("Admin", "admin-dashboard@example.com",
                passwordEncoder.encode("clave-segura-123"));
        administrador.setRol(RolUsuario.ADMINISTRADOR);
        usuarioRepository.save(administrador);

        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin-dashboard@example.com","password":"clave-segura-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(respuesta, "$.accessToken");

        mockMvc.perform(get("/api/reportes/metricas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void filtrosDeFecha_debenIncluirElDiaFinalEnListasYMetricas() throws Exception {
        Usuario ciudadano = new Usuario("Ciudadano", "ciudadano-fechas@example.com",
                passwordEncoder.encode("clave-segura-123"));
        usuarioRepository.save(ciudadano);

        ReporteIncidente reporte = new ReporteIncidente();
        reporte.setTitulo("Reporte del día");
        reporte.setDescripcion("Descripción");
        reporte.setTipo(TipoReporte.ROBO);
        reporte.setPrioridad(PrioridadReporte.ALTA);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setDireccion("Calle 1");
        reporte.setUsuario(ciudadano);
        reporte = reporteRepository.save(reporte);

        mockMvc.perform(get("/api/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reporte.getId()));

        String fecha = LocalDate.now().toString();
        mockMvc.perform(get("/api/reportes").param("desde", fecha).param("hasta", fecha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reporte.getId()));

        mockMvc.perform(get("/api/reportes/metricas")
                        .param("desde", fecha)
                        .param("hasta", fecha)
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.porEstado.PENDIENTE").value(1))
                .andExpect(jsonPath("$.porPrioridad.ALTA").value(1))
                .andExpect(jsonPath("$.porTipo.ROBO").value(1));

        mockMvc.perform(get("/api/reportes/exportar/csv")
                        .param("desde", fecha)
                        .param("hasta", fecha)
                        .header("Authorization", "Bearer " + crearToken("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"Reporte del día\"")));
    }

    private String crearToken(String rol) {
        return crearToken(rol, "prueba@example.com");
    }

    private String crearToken(String rol, String email) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ciudad-segura")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(5, ChronoUnit.MINUTES))
                .subject(email)
                .claim("scope", rol)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private Usuario guardarUsuario(String email, RolUsuario rol) {
        Usuario usuario = new Usuario("Usuario", email, passwordEncoder.encode("clave-segura-123"));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    private ReporteIncidente guardarReporte(Usuario propietario, String titulo) {
        ReporteIncidente reporte = new ReporteIncidente();
        reporte.setTitulo(titulo);
        reporte.setDescripcion("Descripción del incidente");
        reporte.setTipo(TipoReporte.ROBO);
        reporte.setPrioridad(PrioridadReporte.ALTA);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setDireccion("Calle 1");
        reporte.setUsuario(propietario);
        return reporteRepository.save(reporte);
    }
}
