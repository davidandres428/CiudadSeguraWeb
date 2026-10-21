package com.ciudadsegura.service;

import com.ciudadsegura.dto.SeedMasivoResponse;
import com.ciudadsegura.exception.LoteSemillaYaEjecutadoException;
import com.ciudadsegura.model.Alerta;
import com.ciudadsegura.model.LoteSemilla;
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
import com.ciudadsegura.repository.LoteSemillaRepository;
import com.ciudadsegura.repository.ReporteIncidenteRepository;
import com.ciudadsegura.repository.UsuarioRepository;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SeedMasivoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedMasivoService.class);
    private static final String LOTE_MASIVO = "bogota-mass-seed-v1";

    private final UsuarioRepository usuarioRepository;
    private final ZonaRiesgoRepository zonaRiesgoRepository;
    private final ReporteIncidenteRepository reporteRepository;
    private final AlertaRepository alertaRepository;
    private final LoteSemillaRepository loteSemillaRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedMasivoService(
            UsuarioRepository usuarioRepository,
            ZonaRiesgoRepository zonaRiesgoRepository,
            ReporteIncidenteRepository reporteRepository,
            AlertaRepository alertaRepository,
            LoteSemillaRepository loteSemillaRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRiesgoRepository = zonaRiesgoRepository;
        this.reporteRepository = reporteRepository;
        this.alertaRepository = alertaRepository;
        this.loteSemillaRepository = loteSemillaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SeedMasivoResponse sembrar() {
        if (loteSemillaRepository.existsById(LOTE_MASIVO)) {
            throw new LoteSemillaYaEjecutadoException("El sembrado masivo ya fue ejecutado anteriormente.");
        }
        try {
            loteSemillaRepository.saveAndFlush(new LoteSemilla(LOTE_MASIVO));
        } catch (DataIntegrityViolationException ex) {
            throw new LoteSemillaYaEjecutadoException("El sembrado masivo ya fue ejecutado anteriormente.");
        }

        List<Usuario> usuarios = crearUsuarios();
        List<ZonaRiesgo> zonas = zonaRiesgoRepository.saveAll(crearZonas());
        reporteRepository.saveAll(crearReportes(usuarios, zonas));
        alertaRepository.saveAll(crearAlertas(zonas));

        LOGGER.info("Sembrado masivo completado: usuarios={}, zonas={}, reportes={}, alertas={}",
                usuarios.size(), zonas.size(), 30, 12);
        return new SeedMasivoResponse(
                usuarios.size(), zonas.size(), 30, 12, "Lote de datos de demostración creado correctamente.");
    }

    private List<Usuario> crearUsuarios() {
        String lote = UUID.randomUUID().toString().substring(0, 8);
        return usuarioRepository.saveAll(List.of(
                crearUsuario("Valentina Gómez", "valentina.gomez", lote),
                crearUsuario("Mateo Rodríguez", "mateo.rodriguez", lote),
                crearUsuario("Isabella Martínez", "isabella.martinez", lote),
                crearUsuario("Santiago Hernández", "santiago.hernandez", lote),
                crearUsuario("Mariana López", "mariana.lopez", lote),
                crearUsuario("Sebastián Torres", "sebastian.torres", lote),
                crearUsuario("Gabriela Ramírez", "gabriela.ramirez", lote),
                crearUsuario("Nicolás Castro", "nicolas.castro", lote),
                crearUsuario("Daniela Vargas", "daniela.vargas", lote),
                crearUsuario("Samuel Moreno", "samuel.moreno", lote)));
    }

    private Usuario crearUsuario(String nombre, String alias, String lote) {
        Usuario usuario = new Usuario(nombre, alias + "." + lote + "@demo.ciudadsegura.test",
                passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setRol(RolUsuario.CIUDADANO);
        return usuario;
    }

    private List<ZonaRiesgo> crearZonas() {
        return List.of(
                crearZona("Chapinero — Parque de los Hippies", "Concentración peatonal y reportes de hurto en horas nocturnas; se recomienda transitar por vías principales iluminadas.", NivelRiesgo.ALTO, 4.6408, -74.0632),
                crearZona("Chapinero — Zona G", "Sector gastronómico con alta actividad nocturna y congestión en vías secundarias; atención a pertenencias y movilidad.", NivelRiesgo.MEDIO, 4.6582, -74.0551),
                crearZona("Suba — Plaza Imperial", "Afluencia alta alrededor del centro comercial y paraderos; reforzar el autocuidado en entradas y cruces.", NivelRiesgo.MEDIO, 4.7411, -74.0877),
                crearZona("Suba — Parque Mirador de los Nevados", "Área residencial y recreativa con tramos de baja circulación después del atardecer.", NivelRiesgo.BAJO, 4.7489, -74.0811),
                crearZona("Kennedy — Portal de las Américas", "Movilidad intensa y cruces peatonales concurridos; se registran reportes de hurto y luminarias pendientes.", NivelRiesgo.ALTO, 4.6297, -74.1706),
                crearZona("Kennedy — Parque Mundo Aventura", "Zona de recreación con picos de afluencia en fines de semana y eventos.", NivelRiesgo.MEDIO, 4.6187, -74.1376),
                crearZona("Usaquén — Parque de Usaquén", "Entorno comercial y turístico; se recomienda especial cuidado en horarios de cierre de establecimientos.", NivelRiesgo.MEDIO, 4.6953, -74.0302),
                crearZona("Teusaquillo — Parque Simón Bolívar", "Espacio de actividad deportiva con sectores tranquilos fuera de horarios de eventos.", NivelRiesgo.BAJO, 4.6584, -74.0938),
                crearZona("Bosa — Estación Bosa", "Flujo alto de usuarios del transporte y comercio informal en accesos; conservar rutas peatonales despejadas.", NivelRiesgo.ALTO, 4.6172, -74.1886),
                crearZona("Centro — Plaza de Bolívar", "Centro institucional y turístico; cambia la afluencia durante manifestaciones y eventos públicos.", NivelRiesgo.MEDIO, 4.5981, -74.0758));
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

    private List<ReporteIncidente> crearReportes(List<Usuario> usuarios, List<ZonaRiesgo> zonas) {
        List<ReporteSpec> specs = List.of(
                new ReporteSpec("Hurto de teléfono en paradero", "Un ciudadano reporta el hurto de su teléfono al abordar el transporte.", TipoReporte.ROBO, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION, "Carrera 7 con calle 60", 4.6472, -74.0622, 0, 0, 1),
                new ReporteSpec("Luminaria apagada en cruce", "El cruce permanece sin iluminación durante la noche y reduce la visibilidad peatonal.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.MEDIA, EstadoReporte.PENDIENTE, "Calle 65 con carrera 13", 4.6519, -74.0628, 1, 1, 2),
                new ReporteSpec("Daño de señal de tránsito", "La señal presenta daños y está parcialmente inclinada hacia la calzada.", TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Carrera 5 con calle 56", 4.6409, -74.0637, 2, 0, 12),
                new ReporteSpec("Persona sospechosa en acceso comercial", "Comerciantes solicitan acompañamiento preventivo por comportamiento inusual.", TipoReporte.SOSPECHA, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Calle 69A con carrera 4", 4.6590, -74.0548, 3, 1, 3),
                new ReporteSpec("Venta de estupefacientes reportada", "Residentes reportan actividad sospechosa en un callejón y solicitan verificación.", TipoReporte.VENTA_ESTUPEFACIENTES, PrioridadReporte.ALTA, EstadoReporte.PENDIENTE, "Carrera 6 con calle 58", 4.6431, -74.0648, 4, 0, 1),
                new ReporteSpec("Ruido excesivo en establecimiento", "Se reporta ruido persistente después del horario permitido.", TipoReporte.RUIDO_EXCESIVO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Calle 70 con carrera 5", 4.6606, -74.0540, 5, 1, 14),
                new ReporteSpec("Hurto de bicicleta", "La bicicleta fue sustraída de un soporte próximo a la entrada principal.", TipoReporte.ROBO, PrioridadReporte.ALTA, EstadoReporte.PENDIENTE, "Plaza Imperial, acceso occidental", 4.7416, -74.0884, 6, 2, 4),
                new ReporteSpec("Luminaria intermitente en sendero", "La luminaria se apaga por intervalos y deja un tramo del sendero oscuro.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Parque Mirador de los Nevados, sendero sur", 4.7481, -74.0818, 7, 3, 5),
                new ReporteSpec("Vandalismo en mobiliario público", "Se reportan grafitis recientes en una banca y una señal informativa.", TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Calle 145 con carrera 104", 4.7418, -74.0869, 8, 2, 16),
                new ReporteSpec("Sospecha de seguimiento a peatones", "Un residente solicita verificación preventiva cerca del paso peatonal.", TipoReporte.SOSPECHA, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Avenida Suba con calle 132", 4.7410, -74.0870, 9, 3, 2),
                new ReporteSpec("Robo de bolso en salida del portal", "La víctima reporta que dos personas le arrebataron el bolso al salir del portal.", TipoReporte.ROBO, PrioridadReporte.URGENTE, EstadoReporte.PENDIENTE, "Portal de las Américas, salida norte", 4.6302, -74.1714, 0, 4, 1),
                new ReporteSpec("Luminaria dañada en vía residencial", "Poste con luminaria apagada desde hace varios días; afecta el paso peatonal.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Calle 42 Sur con carrera 81", 4.6189, -74.1381, 1, 5, 4),
                new ReporteSpec("Vandalismo en parada de bus", "La cubierta de la parada presenta daños que exponen a los usuarios a la lluvia.", TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Avenida Ciudad de Cali con calle 6", 4.6282, -74.1695, 2, 4, 18),
                new ReporteSpec("Venta de estupefacientes reportada", "Se solicita presencia preventiva en la zona de acceso al parque.", TipoReporte.VENTA_ESTUPEFACIENTES, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION, "Carrera 79 con calle 6A", 4.6196, -74.1371, 3, 5, 3),
                new ReporteSpec("Ruido excesivo en zona de bares", "Vecinos reportan música elevada y ruido de vehículos durante la madrugada.", TipoReporte.RUIDO_EXCESIVO, PrioridadReporte.MEDIA, EstadoReporte.PENDIENTE, "Calle 64 con carrera 13", 4.6502, -74.0632, 4, 1, 2),
                new ReporteSpec("Sospecha de intento de apertura de vehículo", "Un residente observa manipulación de la cerradura de un vehículo estacionado.", TipoReporte.SOSPECHA, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION, "Calle 119 con carrera 6A", 4.6958, -74.0308, 5, 6, 1),
                new ReporteSpec("Hurto de accesorios de vehículo", "El propietario reporta la sustracción de espejos durante la noche.", TipoReporte.ROBO, PrioridadReporte.MEDIA, EstadoReporte.RESUELTO, "Carrera 7 con calle 118", 4.6947, -74.0296, 6, 6, 21),
                new ReporteSpec("Luminaria apagada en parque", "Luminaria del costado occidental sin servicio; se requiere revisión técnica.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.BAJA, EstadoReporte.PENDIENTE, "Parque de Usaquén, costado occidental", 4.6960, -74.0310, 7, 6, 6),
                new ReporteSpec("Daño en cerramiento del parque", "Se detectó una sección del cerramiento desprendida junto al acceso peatonal.", TipoReporte.VANDALISMO, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Parque Simón Bolívar, acceso calle 53", 4.6588, -74.0946, 8, 7, 3),
                new ReporteSpec("Ruido excesivo en evento comunitario", "Se solicita mediación por ruido que supera el horario acordado con residentes.", TipoReporte.RUIDO_EXCESIVO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Carrera 50 con calle 53", 4.6577, -74.0931, 9, 7, 19),
                new ReporteSpec("Hurto a usuario de transporte", "Un usuario reporta el hurto de su billetera en el acceso a la estación.", TipoReporte.ROBO, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION, "Estación Bosa, acceso oriental", 4.6177, -74.1879, 0, 8, 2),
                new ReporteSpec("Sospecha de venta de sustancias", "Comunidad reporta movimientos sospechosos cerca de un lote sin cerramiento.", TipoReporte.SOSPECHA, PrioridadReporte.MEDIA, EstadoReporte.PENDIENTE, "Calle 65 Sur con carrera 78", 4.6164, -74.1892, 1, 8, 5),
                new ReporteSpec("Vandalismo en señal de nomenclatura", "La señal fue arrancada de su soporte y obstruye parcialmente el andén.", TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Calle 64 Sur con carrera 80", 4.6181, -74.1885, 2, 8, 24),
                new ReporteSpec("Luminaria con cable expuesto", "Se observa daño en luminaria con cableado expuesto; se solicita atención prioritaria.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.ALTA, EstadoReporte.PENDIENTE, "Carrera 79 con calle 63 Sur", 4.6170, -74.1870, 3, 8, 1),
                new ReporteSpec("Ruido excesivo en zona residencial", "Se reporta ruido de maquinaria fuera del horario diurno.", TipoReporte.RUIDO_EXCESIVO, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Calle 12 con carrera 2", 4.5978, -74.0752, 4, 9, 4),
                new ReporteSpec("Sospecha de objeto abandonado", "Un elemento permanece sin supervisión junto a una zona de circulación.", TipoReporte.SOSPECHA, PrioridadReporte.ALTA, EstadoReporte.EN_REVISION, "Plaza de Bolívar, costado sur", 4.5976, -74.0760, 5, 9, 1),
                new ReporteSpec("Hurto de pertenencias en zona turística", "Visitantes reportan la sustracción de pertenencias durante una aglomeración.", TipoReporte.ROBO, PrioridadReporte.MEDIA, EstadoReporte.PENDIENTE, "Carrera 7 con calle 11", 4.5988, -74.0757, 6, 9, 3),
                new ReporteSpec("Vandalismo en contenedor de residuos", "El contenedor fue volcado y varios residuos quedaron sobre el andén.", TipoReporte.VANDALISMO, PrioridadReporte.BAJA, EstadoReporte.RESUELTO, "Carrera 8 con calle 12", 4.5970, -74.0749, 7, 9, 25),
                new ReporteSpec("Venta de estupefacientes reportada", "Habitantes solicitan acompañamiento preventivo en el pasaje peatonal.", TipoReporte.VENTA_ESTUPEFACIENTES, PrioridadReporte.ALTA, EstadoReporte.PENDIENTE, "Calle 10 con carrera 4", 4.5975, -74.0719, 8, 9, 2),
                new ReporteSpec("Luminaria apagada en sendero peatonal", "El tramo entre el parque y la vía principal requiere reposición de luminaria.", TipoReporte.ILUMINACION_DEFICIENTE, PrioridadReporte.MEDIA, EstadoReporte.EN_REVISION, "Calle 62 con carrera 7", 4.6460, -74.0630, 9, 0, 5));

        return specs.stream().map(spec -> {
            ReporteIncidente reporte = new ReporteIncidente();
            reporte.setTitulo(spec.titulo());
            reporte.setDescripcion(spec.descripcion());
            reporte.setTipo(spec.tipo());
            reporte.setPrioridad(spec.prioridad());
            reporte.setEstado(spec.estado());
            reporte.setDireccion(spec.direccion() + " — " + zonas.get(spec.zonaIndice()).getNombre());
            reporte.setLatitud(spec.latitud());
            reporte.setLongitud(spec.longitud());
            reporte.setUsuario(usuarios.get(spec.usuarioIndice()));
            reporte.setFechaReporte(LocalDateTime.now().minusDays(spec.diasAtras()));
            return reporte;
        }).toList();
    }

    private List<Alerta> crearAlertas(List<ZonaRiesgo> zonas) {
        return List.of(
                crearAlerta("Precaución por reportes de hurto", "Mantenga sus pertenencias seguras y prefiera vías transitadas.", "SEGURIDAD", SeveridadAlerta.ALTA, true, zonas.get(0), 1),
                crearAlerta("Recomendación para establecimientos nocturnos", "Acordar rutas de salida y reportar luminarias apagadas.", "PREVENCION", SeveridadAlerta.MEDIA, true, zonas.get(1), 2),
                crearAlerta("Jornada de acompañamiento comunitario", "Punto de orientación y apoyo a usuarios del sector.", "COMUNITARIA", SeveridadAlerta.BAJA, false, zonas.get(2), 8),
                crearAlerta("Mantenimiento de luminarias", "Cuadrilla técnica revisará el sendero del parque durante la mañana.", "SERVICIO_PUBLICO", SeveridadAlerta.MEDIA, true, zonas.get(3), 3),
                crearAlerta("Apoyo preventivo en el portal", "Personal de orientación acompaña el flujo peatonal en horas pico.", "MOVILIDAD", SeveridadAlerta.ALTA, true, zonas.get(4), 1),
                crearAlerta("Evento recreativo en Mundo Aventura", "Se prevé alta afluencia; siga las indicaciones de movilidad.", "COMUNITARIA", SeveridadAlerta.BAJA, false, zonas.get(5), 12),
                crearAlerta("Autocuidado en zona comercial", "Evite dejar objetos visibles en vehículos y cuide sus pertenencias.", "PREVENCION", SeveridadAlerta.MEDIA, true, zonas.get(6), 4),
                crearAlerta("Cierre de jornada deportiva", "Utilice accesos habilitados e iluminados al salir del parque.", "MOVILIDAD", SeveridadAlerta.BAJA, true, zonas.get(7), 2),
                crearAlerta("Reporte preventivo en estación", "Use los pasos peatonales y reporte situaciones de riesgo al personal.", "SEGURIDAD", SeveridadAlerta.CRITICA, true, zonas.get(8), 1),
                crearAlerta("Ajuste de tránsito en el centro", "Consulte cierres temporales y conserve libres los corredores peatonales.", "MOVILIDAD", SeveridadAlerta.MEDIA, false, zonas.get(9), 15),
                crearAlerta("Campaña contra el ruido excesivo", "Se recuerda respetar los horarios de descanso de los residentes.", "CONVIVENCIA", SeveridadAlerta.BAJA, true, zonas.get(1), 6),
                crearAlerta("Canal comunitario de reporte", "Comparta información verificable y evite difundir datos personales.", "COMUNITARIA", SeveridadAlerta.ALTA, true, zonas.get(9), 3));
    }

    private Alerta crearAlerta(
            String titulo,
            String mensaje,
            String tipo,
            SeveridadAlerta severidad,
            boolean activa,
            ZonaRiesgo zona,
            int diasAtras) {
        Alerta alerta = new Alerta();
        alerta.setTitulo(titulo);
        alerta.setMensaje(mensaje);
        alerta.setTipo(tipo);
        alerta.setSeveridad(severidad);
        alerta.setActiva(activa);
        alerta.setZonaRiesgo(zona);
        alerta.setFechaCreacion(LocalDateTime.now().minusDays(diasAtras));
        return alerta;
    }

    private record ReporteSpec(
            String titulo,
            String descripcion,
            TipoReporte tipo,
            PrioridadReporte prioridad,
            EstadoReporte estado,
            String direccion,
            double latitud,
            double longitud,
            int usuarioIndice,
            int zonaIndice,
            int diasAtras) {
    }
}
