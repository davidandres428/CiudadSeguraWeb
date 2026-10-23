package com.ciudadsegura.config;

import com.ciudadsegura.CiudadSeguraApplication;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = CiudadSeguraApplication.class,
        properties = {
                "app.seed.enabled=true",
                "app.seed.admin-password=",
                "spring.datasource.url=jdbc:h2:mem:seed_without_admin_password;DB_CLOSE_DELAY=-1"
        })
class DataInitializerSinClaveAdminTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @Autowired
    private ReporteIncidenteRepository reporteRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Test
    void arrancaYSiembraDatosSinVariableDeClaveAdministrativa() {
        var admin = usuarioRepository.findByEmail("david.andresgalvis@gmail.com").orElseThrow();

        assertEquals(RolUsuario.ADMINISTRADOR, admin.getRol());
        assertTrue(admin.getPassword().startsWith("$2"));
        assertEquals(15, zonaRiesgoRepository.count());
        assertEquals(128, reporteRepository.count());
        assertEquals(3, alertaRepository.count());
    }
}
