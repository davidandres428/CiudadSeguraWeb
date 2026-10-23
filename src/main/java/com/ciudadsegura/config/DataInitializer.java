package com.ciudadsegura.config;

import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.model.Noticia;
import com.ciudadsegura.model.ReporteIncidente;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.EstadoReporte;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.model.enums.PrioridadReporte;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.model.enums.SeveridadAlerta;
import com.ciudadsegura.model.enums.TipoReporte;
import com.ciudadsegura.repository.AlertaRepository;
import com.ciudadsegura.repository.NoticiaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import com.ciudadsegura.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final ReporteIncidenteRepository reporteRepository;
    private final AlertaRepository alertaRepository;
    private final NoticiaRepository noticiaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            ZonaRiesgoRepository zonaRiesgoRepository,
            ReporteIncidenteRepository reporteRepository,
            AlertaRepository alertaRepository,
            NoticiaRepository noticiaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.reporteRepository = reporteRepository;
        this.alertaRepository = alertaRepository;
        this.noticiaRepository = noticiaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Usuario administrador = usuarioRepository.findByEmail(AuthService.ADMINISTRADOR_PRINCIPAL).orElse(null);
        if (administrador == null) {
            String passwordInicial = adminPassword == null || adminPassword.isBlank()
                    ? UUID.randomUUID().toString()
                    : adminPassword;
            administrador = crearUsuario(
                    "David Andrés Galvis", AuthService.ADMINISTRADOR_PRINCIPAL,
                    RolUsuario.ADMINISTRADOR, passwordInicial);
            if (adminPassword == null || adminPassword.isBlank()) {
                LOGGER.warn("Se creó la cuenta administradora principal sin contraseña conocida. "
                        + "Configura SEED_ADMIN_PASSWORD para acceso por contraseña o usa OAuth con correo verificado.");
            }
        } else {
            administrador.setRol(RolUsuario.ADMINISTRADOR);
            if (adminPassword != null && !adminPassword.isBlank()) {
                administrador.setPassword(passwordEncoder.encode(adminPassword));
            }
        }
        usuarioRepository.save(administrador);

        List<ZonaRiesgo> zonas = zonaRiesgoRepository.findAll();
        if (zonas.isEmpty()) {
            zonas = zonaRiesgoRepository.saveAll(crearZonas());
        }

        if (reporteRepository.count() == 0) {
            List<Usuario> ciudadanos = asegurarCiudadanosDemostracion();
            reporteRepository.saveAll(crearReportes(ciudadanos, zonas));
        }

        if (alertaRepository.count() == 0) {
            alertaRepository.saveAll(crearAlertas(zonas));
        }
        if (noticiaRepository.count() == 0) {
            noticiaRepository.saveAll(crearNoticias());
        }
    }

    private List<Usuario> asegurarCiudadanosDemostracion() {
        List<Usuario> ciudadanos = new ArrayList<>();
        String[][] datos = {
                {"Laura Méndez", "laura.mendez@ciudadsegura.test"},
                {"Andrés Castillo", "andres.castillo@ciudadsegura.test"},
                {"Camila Rojas", "camila.rojas@ciudadsegura.test"}
        };
        for (String[] dato : datos) {
            Usuario ciudadano = usuarioRepository.findByEmail(dato[1]).orElseGet(() ->
                    crearUsuario(dato[0], dato[1], RolUsuario.CIUDADANO, UUID.randomUUID().toString()));
            ciudadanos.add(usuarioRepository.save(ciudadano));
        }
        return ciudadanos;
    }

    private Usuario crearUsuario(String nombre, String email, RolUsuario rol, String password) {
        Usuario usuario = new Usuario(nombre, email, passwordEncoder.encode(password));
        usuario.setRol(rol);
        return usuario;
    }

    private List<ZonaRiesgo> crearZonas() {
        List<ZonaSpec> zonas = List.of(
                new ZonaSpec("Chapinero - Parque de los Hippies", "Entorno de alta afluencia nocturna; priorizar senderos iluminados.", NivelRiesgo.ALTO, 4.6408, -74.0632, "OPERATIVAS (6/6)", 2),
                new ZonaSpec("Chapinero - Zona G", "Sector gastronómico con movimiento peatonal hasta el cierre de establecimientos.", NivelRiesgo.MEDIO, 4.6582, -74.0551, "OPERATIVAS (5/5)", 5),
                new ZonaSpec("Suba - Plaza Imperial", "Afluencia alta alrededor del centro comercial y paraderos del sector.", NivelRiesgo.MEDIO, 4.7411, -74.0877, "MONITOREO PARCIAL (4/5)", 8),
                new ZonaSpec("Suba - Parque Mirador de los Nevados", "Zona residencial y recreativa con menor circulación después del atardecer.", NivelRiesgo.BAJO, 4.7489, -74.0811, "OPERATIVAS (4/4)", 3),
                new ZonaSpec("Kennedy - Portal de las Américas", "Cruces concurridos y alto flujo de usuarios del transporte público.", NivelRiesgo.ALTO, 4.6297, -74.1706, "EN MANTENIMIENTO (4/5)", 1),
                new ZonaSpec("Kennedy - Parque Mundo Aventura", "Picos de afluencia durante fines de semana y eventos familiares.", NivelRiesgo.MEDIO, 4.6187, -74.1376, "OPERATIVAS (5/5)", 6),
                new ZonaSpec("Usaquén - Parque principal", "Entorno comercial y turístico; reforzar autocuidado en horarios de cierre.", NivelRiesgo.MEDIO, 4.6953, -74.0302, "OPERATIVAS (4/4)", 4),
                new ZonaSpec("Teusaquillo - Parque Simón Bolívar", "Actividad deportiva y eventos con cambios de afluencia durante el día.", NivelRiesgo.BAJO, 4.6584, -74.0938, "OPERATIVAS (7/7)", 2),
                new ZonaSpec("Bosa - Estación Bosa", "Flujo alto en accesos y comercio; mantener despejadas las rutas peatonales.", NivelRiesgo.ALTO, 4.6172, -74.1886, "MONITOREO PARCIAL (3/5)", 11),
                new ZonaSpec("Santa Fe - Plaza de Bolívar", "Zona institucional y turística con alta concentración en eventos públicos.", NivelRiesgo.MEDIO, 4.5981, -74.0758, "OPERATIVAS (6/6)", 7),
                new ZonaSpec("Santa Fe - Parque Nacional", "Tramos de baja circulación en horas tempranas y al anochecer.", NivelRiesgo.ALTO, 4.6258, -74.0628, "EN MANTENIMIENTO (3/4)", 9),
                new ZonaSpec("Engativá - Jardín Botánico", "Entorno recreativo y cultural con circulación organizada en horario diurno.", NivelRiesgo.BAJO, 4.6687, -74.1001, "OPERATIVAS (5/5)", 3),
                new ZonaSpec("Fontibón - Parque Fundacional", "Actividad comercial local y afluencia variable en vías de conexión.", NivelRiesgo.MEDIO, 4.6782, -74.1421, "OPERATIVAS (4/4)", 6),
                new ZonaSpec("Tunjuelito - Parque El Tunal", "Zona deportiva con mayor presencia de familias durante fines de semana.", NivelRiesgo.BAJO, 4.5723, -74.1304, "OPERATIVAS (6/6)", 4),
                new ZonaSpec("San Cristóbal - Parque Altamira", "Sector residencial de ladera con puntos de visibilidad limitada.", NivelRiesgo.ALTO, 4.5594, -74.0897, "MONITOREO PARCIAL (3/4)", 12));

        LocalDate hoy = LocalDate.now();
        return zonas.stream().map(spec -> {
            ZonaRiesgo zona = new ZonaRiesgo();
            zona.setNombre(spec.nombre());
            zona.setDescripcion(spec.descripcion());
            zona.setNivelRiesgo(spec.nivel());
            zona.setLatitud(spec.latitud());
            zona.setLongitud(spec.longitud());
            zona.setEstadoCamaras(spec.estadoCamaras());
            zona.setUltimaInspeccion(hoy.minusDays(spec.diasDesdeInspeccion()));
            return zona;
        }).toList();
    }

    private List<ReporteIncidente> crearReportes(List<Usuario> usuarios, List<ZonaRiesgo> zonas) {
        if (zonas.isEmpty()) {
            throw new IllegalStateException("No es posible sembrar reportes sin zonas disponibles.");
        }
        TipoReporte[] tipos = {
                TipoReporte.ROBO, TipoReporte.VANDALISMO, TipoReporte.DISTURBIO,
                TipoReporte.ACCIDENTE, TipoReporte.ILUMINACION_DEFICIENTE,
                TipoReporte.SOSPECHA, TipoReporte.RUIDO_EXCESIVO,
                TipoReporte.VENTA_ESTUPEFACIENTES
        };
        PrioridadReporte[] prioridades = PrioridadReporte.values();
        EstadoReporte[] estados = {
                EstadoReporte.PENDIENTE, EstadoReporte.EN_REVISION,
                EstadoReporte.RESUELTO, EstadoReporte.PENDIENTE,
                EstadoReporte.RESUELTO
        };
        String[] descripciones = {
                "La comunidad solicita verificación del hecho y acompañamiento preventivo en el sector.",
                "Se adjuntó una referencia de ubicación para facilitar la atención por el equipo territorial.",
                "El reporte fue recibido por vecinos y requiere seguimiento durante la jornada.",
                "Se recomienda conservar distancia y comunicarse con la línea de emergencias si hay riesgo inmediato."
        };
        LocalDateTime ahora = LocalDateTime.now();
        int diasDisponiblesEsteMes = Math.min(ahora.getDayOfMonth(), 28);
        List<ReporteIncidente> reportes = new ArrayList<>(128);

        for (int i = 0; i < 128; i++) {
            ZonaRiesgo zona = zonas.get(i % zonas.size());
            TipoReporte tipo = tipos[i % tipos.length];
            String titulo = tituloReporte(tipo) + " - " + zona.getNombre() + " - caso " + (i + 1);
            ReporteIncidente reporte = new ReporteIncidente();
            reporte.setTitulo(titulo);
            reporte.setDescripcion(descripciones[i % descripciones.length]);
            reporte.setTipo(tipo);
            reporte.setPrioridad(prioridades[i % prioridades.length]);
            reporte.setEstado(estados[i % estados.length]);
            reporte.setDireccion("Sector " + zona.getNombre() + ", acceso " + (char) ('A' + i % 6));
            reporte.setLatitud(zona.getLatitud() + ((i % 5) - 2) * 0.0003);
            reporte.setLongitud(zona.getLongitud() + ((i % 7) - 3) * 0.0003);
            reporte.setUsuario(usuarios.get(i % usuarios.size()));
            reporte.setFechaReporte(ahora.minusDays(i % diasDisponiblesEsteMes).minusHours(i % 24));
            reportes.add(reporte);
        }
        return reportes;
    }

    private String tituloReporte(TipoReporte tipo) {
        return switch (tipo) {
            case ROBO -> "Hurto reportado en vía pública";
            case VANDALISMO -> "Daño a mobiliario urbano";
            case DISTURBIO -> "Perturbación de la convivencia";
            case ACCIDENTE -> "Incidente vial reportado";
            case ILUMINACION_DEFICIENTE -> "Luminaria fuera de servicio";
            case SOSPECHA -> "Solicitud de verificación preventiva";
            case RUIDO_EXCESIVO -> "Reporte de ruido excesivo";
            case VENTA_ESTUPEFACIENTES -> "Actividad sospechosa reportada";
        };
    }

    private List<Alerta> crearAlertas(List<ZonaRiesgo> zonas) {
        return List.of(
                crearAlerta("Prevención de hurtos en zonas concurridas",
                        "Mantén tus pertenencias a la vista y utiliza rutas iluminadas. Reporta cualquier emergencia a la línea 123.",
                        "PREVENTIVA", SeveridadAlerta.ALTA, zonas.get(0), 0),
                crearAlerta("Atención a cierres y desvíos viales",
                        "Se recomienda planear el recorrido y seguir las indicaciones de movilidad en los accesos del sector.",
                        "VIAL", SeveridadAlerta.MEDIA, zonas.get(Math.min(4, zonas.size() - 1)), 1),
                crearAlerta("Recomendación por lluvias y tormenta eléctrica",
                        "Evita permanecer bajo árboles durante la tormenta y reporta encharcamientos que afecten el paso peatonal.",
                        "CLIMATICA", SeveridadAlerta.ALTA, zonas.get(Math.min(9, zonas.size() - 1)), 0));
    }

    private Alerta crearAlerta(
            String titulo, String mensaje, String tipo, SeveridadAlerta severidad, ZonaRiesgo zona, int diasAtras) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setMensaje(mensaje);
        alerta.setTipo(tipo);
        alerta.setSeveridad(severidad);
        alerta.setActiva(true);
        alerta.setZonaRiesgo(zona);
        alerta.setFechaCreacion(LocalDateTime.now().minusDays(diasAtras));
        return alerta;
    }

    private List<Noticia> crearNoticias() {
        LocalDateTime ahora = LocalDateTime.now();
        return List.of(
                crearNoticia("Jornada comunitaria de prevención en Chapinero", "Comunidad",
                        "La Alcaldía Local invita a vecinos y comerciantes a una jornada de prevención, orientación y reporte seguro en el Parque de los Hippies.",
                        ahora.minusDays(1)),
                crearNoticia("Recomendaciones para desplazarse con seguridad", "Prevención",
                        "Planea tus recorridos, prefiere vías iluminadas y evita compartir en tiempo real información personal o de ubicación en redes sociales.",
                        ahora.minusDays(2)),
                crearNoticia("Inspección de cámaras y luminarias en Kennedy", "Monitoreo",
                        "Equipos territoriales adelantan revisión preventiva de cámaras y luminarias cercanas a los principales accesos del Portal de las Américas.",
                        ahora.minusDays(3)),
                crearNoticia("Canales oficiales para reportar emergencias", "Comunicado",
                        "Ante una emergencia inmediata comunícate con la línea 123. Para incidentes no urgentes, utiliza el formulario de reportes de Ciudad Segura.",
                        ahora.minusDays(4)),
                crearNoticia("Encuentro ciudadano sobre convivencia", "Comunidad",
                        "Representantes barriales compartirán iniciativas de convivencia y rutas de atención para fortalecer la colaboración entre vecinos.",
                        ahora.minusDays(5)));
    }

    private Noticia crearNoticia(String titulo, String categoria, String contenido, LocalDateTime fechaPublicacion) {
        Noticia noticia = new Noticia();
        noticia.setTitulo(titulo);
        noticia.setCategoria(categoria);
        noticia.setContenido(contenido);
        noticia.setFechaPublicacion(fechaPublicacion);
        return noticia;
    }

    private record ZonaSpec(
            String nombre,
            String descripcion,
            NivelRiesgo nivel,
            double latitud,
            double longitud,
            String estadoCamaras,
            int diasDesdeInspeccion) {
    }
}
