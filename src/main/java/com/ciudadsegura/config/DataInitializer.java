package com.ciudadsegura.config;

import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile({"default", "local"})
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin.demo@ciudadsegura.local";

    private final UsuarioRepository usuarioRepository;
    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final ReporteIncidenteRepository reporteRepository;
    private final AlertaRepository alertaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            ZonaRiesgoRepository zonaRiesgoRepository,
            ReporteIncidenteRepository reporteRepository,
            AlertaRepository alertaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.reporteRepository = reporteRepository;
        this.alertaRepository = alertaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!baseDeDatosVacia()) {
            return;
        }
        if (adminPassword == null || adminPassword.length() < 12) {
            throw new IllegalStateException(
                    "Define SEED_ADMIN_PASSWORD con al menos 12 caracteres para habilitar los datos de prueba.");
        }

        Usuario administrador = crearUsuario(
                "Administración Ciudad Segura", ADMIN_EMAIL, RolUsuario.ADMINISTRADOR, adminPassword);
        Usuario ciudadanoUno = crearUsuario(
                "Laura Méndez", "laura.mendez@ciudadsegura.test", RolUsuario.CIUDADANO, UUID.randomUUID().toString());
        Usuario ciudadanoDos = crearUsuario(
                "Andrés Castillo", "andres.castillo@ciudadsegura.test", RolUsuario.CIUDADANO, UUID.randomUUID().toString());
        Usuario ciudadanoTres = crearUsuario(
                "Camila Rojas", "camila.rojas@ciudadsegura.test", RolUsuario.CIUDADANO, UUID.randomUUID().toString());
        usuarioRepository.saveAll(List.of(administrador, ciudadanoUno, ciudadanoDos, ciudadanoTres));

        List<ZonaRiesgo> zonas = zonaRiesgoRepository.saveAll(List.of(
                crearZona("Parque de los Periodistas", "Riesgo alto por hurtos reportados en los alrededores.",
                        NivelRiesgo.ALTO, 4.6015, -74.0683),
                crearZona("Estación Calle 72", "Riesgo medio en accesos y zonas de ascenso al transporte.",
                        NivelRiesgo.MEDIO, 4.6584, -74.0595),
                crearZona("Parque El Virrey", "Riesgo bajo; se recomienda mantener iluminación y vigilancia.",
                        NivelRiesgo.BAJO, 4.6752, -74.0565),
                crearZona("Portal de las Américas", "Riesgo alto en cruces peatonales y paraderos cercanos.",
                        NivelRiesgo.ALTO, 4.6294, -74.1704)));

        List<ReporteIncidente> reportes = List.of(
                crearReporte("Hurto de celular en vía pública", "La persona afectada reporta el hurto al salir del parque.",
                        TipoReporte.ROBO, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION,
                        "Carrera 4 con calle 16", 4.6011, -74.0680, ciudadanoUno, 1),
                crearReporte("Daño a luminaria peatonal", "Luminaria fuera de servicio en el acceso oriental.",
                        TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.MEDIA, EstadoReporte.PENDIENTE,
                        "Calle 72 con carrera 11", 4.6588, -74.0602, ciudadanoDos, 2),
                crearReporte("Vandalismo en mobiliario urbano", "Se observan daños recientes en una banca del parque.",
                        TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO,
                        "Parque El Virrey, acceso norte", 4.6760, -74.0569, ciudadanoTres, 3),
                crearReporte("Persona sospechosa junto al paradero", "Vecinos solicitan verificación preventiva del sector.",
                        TipoReporte.SOSPECHA, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION,
                        "Avenida Ciudad de Cali con calle 6", 4.6290, -74.1698, ciudadanoUno, 4),
                crearReporte("Intento de robo a transeúnte", "Se reporta un intento de robo en el cruce peatonal.",
                        TipoReporte.ROBO, PrioridadReporte.URGENTE, EstadoReporte.PENDIENTE,
                        "Portal de las Américas, salida principal", 4.6300, -74.1712, ciudadanoDos, 4),
                crearReporte("Grafiti en señalización pública", "Se reporta vandalismo en señal de orientación urbana.",
                        TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO,
                        "Calle 17 con carrera 3", 4.6020, -74.0674, ciudadanoTres, 1));
        reporteRepository.saveAll(reportes);

        alertaRepository.saveAll(List.of(
                crearAlerta("Precaución por hurtos", "Mantén tus pertenencias seguras y usa los senderos iluminados.",
                        "SEGURIDAD", true, zonas.get(0), 1),
                crearAlerta("Jornada de mantenimiento de luminarias", "Se adelantan trabajos de iluminación en el sector.",
                        "SERVICIO_PUBLICO", true, zonas.get(1), 2),
                crearAlerta("Recomendación preventiva", "Evita dejar objetos visibles dentro de vehículos.",
                        "PREVENCION", false, zonas.get(2), 4),
                crearAlerta("Apoyo a la salida del portal", "Personal de orientación acompaña el flujo peatonal.",
                        "COMUNITARIA", true, zonas.get(3), 1)));
    }

    private boolean baseDeDatosVacia() {
        return usuarioRepository.count() == 0
                && zonaRiesgoRepository.count() == 0
                && reporteRepository.count() == 0
                && alertaRepository.count() == 0;
    }

    private Usuario crearUsuario(String nombre, String email, RolUsuario rol, String password) {
        Usuario usuario = new Usuario(nombre, email, passwordEncoder.encode(password));
        usuario.setRol(rol);
        return usuario;
    }

    private ZonaRiesgo crearZona(
            String nombre, String descripcion, NivelRiesgo nivel, double latitud, double longitud) {
        ZonaRiesgo zona = new ZonaRiesgo();
        zona.setNombre(nombre);
        zona.setDescripcion(descripcion);
        zona.setNivelRiesgo(nivel);
        zona.setLatitud(latitud);
        zona.setLongitud(longitud);
        return zona;
    }

    private ReporteIncidente crearReporte(
            String titulo,
            String descripcion,
            TipoReporte tipo,
            PrioridadReporte prioridad,
            EstadoReporte estado,
            String direccion,
            double latitud,
            double longitud,
            Usuario usuario,
            int diasAtras) {
        ReporteIncidente reporte = new ReporteIncidente();
        reporte.setTitulo(titulo);
        reporte.setDescripcion(descripcion);
        reporte.setTipo(tipo);
        reporte.setPrioridad(prioridad);
        reporte.setEstado(estado);
        reporte.setDireccion(direccion);
        reporte.setLatitud(latitud);
        reporte.setLongitud(longitud);
        reporte.setUsuario(usuario);
        reporte.setFechaReporte(LocalDateTime.now().minusDays(diasAtras));
        return reporte;
    }

    private Alerta crearAlerta(
            String titulo, String mensaje, String tipo, boolean activa, ZonaRiesgo zona, int diasAtras) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setMensaje(mensaje);
        alerta.setTipo(tipo);
        alerta.setActiva(activa);
        alerta.setZonaRiesgo(zona);
        alerta.setFechaCreacion(LocalDateTime.now().minusDays(diasAtras));
        return alerta;
    }
}
