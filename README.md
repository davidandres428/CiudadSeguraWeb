# Ciudad Segura — Aplicación integrada (back-end + front-end + base de datos)

Proyecto formativo: **Construcción de software integrador de tecnologías orientadas a servicios.**
Evidencia: **GA8-220501096-AA1-EV01** — Integrar módulos, de acuerdo con el diseño establecido.

Este proyecto integra en **una sola aplicación Spring Boot** los módulos construidos en guías
anteriores: un menú principal (front-end) que conecta con los servicios REST (back-end) y estos,
a su vez, con la base de datos, de modo que el CRUD de cada módulo se puede
ejecutar desde una interfaz real en el navegador.

## Módulos integrados

1. **Usuarios**: registro e inicio de sesión (contraseña cifrada con BCrypt), con roles de ciudadano o administrador, y CRUD de usuarios.
2. **Reportes de incidentes**: registro de hechos de inseguridad con tipo, prioridad, dirección y descripción; las coordenadas son opcionales.
3. **Zonas de riesgo**: zonas geográficas clasificadas por nivel de riesgo (bajo, medio, alto).
4. **Alertas**: notificaciones emitidas a los ciudadanos, opcionalmente asociadas a una zona de riesgo.
5. **Dashboard administrativo**: gráficos de reportes por estado, prioridad y tipo, con filtros de fechas y exportación CSV.
6. **PQRS**: radicación pública de peticiones, quejas, reclamos y sugerencias, persistida en la base de datos.

El **menú principal** (`index.html`) enlaza los cuatro módulos y cada página consume los mismos
endpoints REST documentados abajo, verificando que la navegación y la conexión a base de datos
funcionen de extremo a extremo.

## Tecnologías

- Java 25 · Spring Boot 4.1.1
- Spring Web (API REST) + front-end estático (HTML, CSS y JavaScript puro) servido desde `src/main/resources/static`
- Spring Data JPA + H2 para desarrollo local y PostgreSQL en el perfil `prod`
- Spring Validation (`@NotBlank`, `@Email`, etc.)
- Spring Security OAuth2 Resource Server y JWT HS256 para proteger métricas y exportación
- BCrypt (`spring-security-crypto`) para cifrado de contraseñas
- JUnit 5 + Mockito (pruebas unitarias de la capa de servicios)

## Estructura del proyecto

```
src/main/java/com/ciudadsegura/
├── CiudadSeguraApplication.java
├── config/          -> configuración de CORS
├── controller/       -> controladores REST (auth, usuarios, reportes, zonas, alertas)
├── dto/              -> objetos de transferencia (requests/responses)
├── exception/        -> excepciones de negocio + manejador global
├── model/            -> entidades JPA y enums
├── repository/       -> interfaces Spring Data JPA
└── service/          -> lógica de negocio de cada módulo

src/main/resources/
├── application.properties
└── static/            -> front-end (menú principal + una página por módulo)
    ├── index.html      -> menú principal, integra los 4 módulos
    ├── login.html       -> registro e inicio de sesión
    ├── usuarios.html
    ├── reportes.html
    ├── zonas.html
    ├── alertas.html
    ├── dashboard.html  -> estadísticas y exportación CSV (administradores)
    ├── css/styles.css
    └── js/api.js         -> cliente REST compartido por todas las páginas

src/test/java/com/ciudadsegura/service/  -> pruebas unitarias (JUnit 5 + Mockito)
```

## Configuración y ejecución

1. La configuración local usa una base H2 en memoria; no requiere instalar una base de datos.
2. Ejecutar:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
3. Abrir el navegador en `http://localhost:8081/` — ahí se carga el **menú principal** con acceso
   a los módulos; la dirección permanece en la raíz.
4. Para registrarte como ciudadano y poder crear reportes, entra a `http://localhost:8081/login`.
5. Para desplegar el front-end por separado en Vercel, configura la URL base pública del back-end en `src/main/resources/static/js/api-config.js` (por ejemplo, `https://mi-api.example.com`, sin `/api` al final).

### Ejecutar con Docker

Empaqueta localmente la aplicación, construye la imagen desde el JAR de la raíz y ejecuta el contenedor:
```bash
mvn clean package -DskipTests
cp target/ciudad-segura-0.0.1-SNAPSHOT.jar .
docker build -t ciudad-segura .
docker run --rm -p 8080:8080 ciudad-segura
```
La imagen ejecuta el JAR ya empaquetado y no compila durante el build de Docker. La aplicación
queda disponible en `http://localhost:8080`; Render puede asignar el puerto mediante `PORT`.
Este comando usa H2 en memoria; los datos no persisten al detener el contenedor. Al desplegar
desde Docker en Render, incluye el JAR de la raíz en Git junto con el Dockerfile.

### Datos de demostración locales

El inicializador está desactivado por defecto y solo se carga sin perfil explícito o con el perfil
`local` (nunca con `prod` ni otros perfiles). Para poblar una
base local vacía con una cuenta administradora, tres ciudadanos, cuatro zonas, seis reportes y
cuatro alertas, configura `SEED_DATA_ENABLED=true` y `SEED_ADMIN_PASSWORD` (mínimo 12 caracteres)
al iniciar la aplicación. La contraseña no se guarda en el código; las contraseñas aleatorias de
los ciudadanos de demostración no son utilizables para iniciar sesión.

PowerShell:
```powershell
$env:SEED_DATA_ENABLED = "true"
$env:SEED_ADMIN_PASSWORD = "elige-una-clave-local-segura"
.\mvnw.cmd spring-boot:run
```

Cuenta inicial de administrador: `admin.demo@ciudadsegura.local`. El sembrado se ejecuta solo si
usuarios, zonas, reportes y alertas están todos vacíos; si cualquiera de esas tablas ya contiene
datos, no modifica ninguna de ellas. Para volver a probarlo en local, usa una base H2 en memoria
vacía o elimina explícitamente los datos locales.

### Despliegue en Railway

No se incluye una configuración específica de Railway en el repositorio. Para producción, configura
el servicio para activar el perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`) y referencia las variables
de una instancia PostgreSQL de Railway: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` y `PGPASSWORD`.
Configura también `JWT_SECRET` con un valor aleatorio de al menos 32 bytes. Railway proporciona
`PORT` para el puerto de escucha. No guardes credenciales ni claves en el repositorio. La aplicación
falla al iniciar si falta el secreto JWT en el perfil `prod`.

> El registro público siempre crea cuentas `CIUDADANO`; el rol enviado por el cliente no se acepta para asignar privilegios. Una vez aprovisionada la primera cuenta administradora, un administrador puede consultar usuarios y asignar roles desde `usuarios.html`. Antes de habilitar el dashboard en una base existente, audita las cuentas que ya tengan rol `ADMINISTRADOR`. Los endpoints de métricas, CSV, usuarios y las escrituras de zonas/alertas requieren JWT de una cuenta administradora. Las escrituras de reportes requieren autenticación; los ciudadanos solo modifican/eliminan los propios y el servidor deriva el propietario desde el JWT. Las lecturas de reportes, zonas y alertas siguen siendo públicas.

El mapa interactivo de reportes permite elegir coordenadas con un clic y presenta reportes y zonas guardadas. La página de zonas muestra las mismas capas sobre Leaflet/OpenStreetMap. El dashboard administrativo y la gestión de usuarios redirigen a usuarios sin el rol autorizado; las API también verifican los permisos en el servidor.

### Radicación PQRS

El formulario público de la página principal guarda cada radicación en PostgreSQL en producción o H2 en local. El identificador devuelto es el número de radicado; no se requiere iniciar sesión.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/pqrs` | Radicar una solicitud pública y retornar su identificador, estado y fecha |

Cuerpo JSON esperado:
```json
{
  "nombre": "Nombre del ciudadano",
  "email": "ciudadano@example.com",
  "tipo": "PETICION",
  "asunto": "Asunto de la solicitud",
  "detalle": "Descripción de la petición, queja, reclamo o sugerencia"
}
```
`tipo` acepta `PETICION`, `QUEJA`, `RECLAMO` o `SUGERENCIA`.

### URLs del sitio

Spring Boot sirve las vistas desde estas rutas sin extensión: `/`, `/reportes`, `/zonas`,
`/alertas`, `/usuarios`, `/dashboard` y `/login`. Las direcciones antiguas con `.html`
redirigen permanentemente a la URL limpia equivalente; `/index.html` redirige a `/`.
Los recursos `/api/**`, `/css/**` y `/js/**` conservan sus rutas.

### CORS

La API habilita CORS con cualquier origen (`allowedOriginPatterns = ["*"]`), permite `GET`, `POST`, `PUT`, `DELETE` y `OPTIONS`, y no acepta credenciales de navegador. El cliente envía el JWT explícitamente en `Authorization: Bearer ...`.

## Ejecutar las pruebas unitarias

```bash
.\mvnw.cmd test
```

### Pruebas de navegador con Playwright

Inicia la aplicación local en `http://127.0.0.1:18081` en una terminal:

```powershell
$env:PORT = "18081"
.\mvnw.cmd spring-boot:run
```

En otra terminal, instala las dependencias del proyecto E2E, instala Chromium si aún no está disponible y ejecuta los flujos de registro, PQRS, reporte geolocalizado, cierre de sesión y rutas protegidas:

```powershell
Set-Location .\e2e-tests
npm ci
npm run install:chromium
npm test
```

Playwright usa su Chromium administrado por defecto. Para usar una instalación existente, define `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH`; para probar otro despliegue local, define `BASE_URL`.

## Endpoints

### Autenticación — `/api/auth`
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/registro` | Registra un ciudadano y retorna usuario más JWT |
| POST | `/api/auth/login` | Valida credenciales y retorna usuario más JWT (vigencia por defecto: 30 minutos) |

### Usuarios — `/api/usuarios`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Consultar todos los usuarios (requiere JWT de administrador) |
| GET | `/api/usuarios/{id}` | Consultar un usuario por id (requiere JWT de administrador) |
| PUT | `/api/usuarios/{id}` | Actualizar un usuario (requiere JWT de administrador) |
| DELETE | `/api/usuarios/{id}` | Eliminar un usuario (requiere JWT de administrador) |

### Reportes de incidentes — `/api/reportes`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/reportes` | Consultar todos los reportes |
| GET | `/api/reportes?usuarioId={id}` | Consultar reportes de un usuario |
| GET | `/api/reportes?estado={PENDIENTE\|EN_REVISION\|RESUELTO\|DESCARTADO}` | Filtrar por estado |
| GET | `/api/reportes?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | Filtrar por fechas (ambos extremos inclusivos; puede combinarse con estado y usuarioId) |
| GET | `/api/reportes/metricas?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | Conteos por estado, prioridad y tipo; requiere JWT de administrador |
| GET | `/api/reportes/exportar/csv?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | Descarga CSV del período; requiere JWT de administrador |
| GET | `/api/reportes/{id}` | Consultar un reporte por id |
| POST | `/api/reportes` | Registrar un reporte autenticado; el servidor asigna el propietario desde el JWT |
| PUT | `/api/reportes/{id}` | Actualizar un reporte propio o cualquiera como administrador |
| DELETE | `/api/reportes/{id}` | Eliminar un reporte propio o cualquiera como administrador |

Cuerpo JSON esperado en POST/PUT:
```json
{
  "titulo": "Robo en la calle 45",
  "descripcion": "Robo a mano armada frente al parque",
  "tipo": "ROBO",
  "prioridad": "ALTA",
  "direccion": "Calle 45 con carrera 7, Bogotá",
  "latitud": 4.710989,
  "longitud": -74.072092
}
```
Los campos `latitud` y `longitud` son opcionales; `titulo`, `descripcion`, `tipo`, `prioridad` y `direccion` son obligatorios.

### Zonas de riesgo — `/api/zonas-riesgo`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/zonas-riesgo` | Consultar todas las zonas |
| GET | `/api/zonas-riesgo?nivel={BAJO\|MEDIO\|ALTO}` | Filtrar por nivel de riesgo |
| GET | `/api/zonas-riesgo/{id}` | Consultar una zona por id |
| POST | `/api/zonas-riesgo` | Registrar una nueva zona (administrador) |
| PUT | `/api/zonas-riesgo/{id}` | Actualizar una zona (administrador) |
| DELETE | `/api/zonas-riesgo/{id}` | Eliminar una zona (administrador) |

Cuerpo JSON esperado en POST/PUT:
```json
{
  "nombre": "Centro histórico",
  "descripcion": "Alta afluencia nocturna, poca iluminación",
  "nivelRiesgo": "ALTO",
  "latitud": 4.598,
  "longitud": -74.076
}
```

### Alertas — `/api/alertas`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/alertas` | Consultar todas las alertas |
| GET | `/api/alertas?activas=true` | Consultar solo alertas activas |
| GET | `/api/alertas/{id}` | Consultar una alerta por id |
| POST | `/api/alertas?zonaRiesgoId={id}` | Registrar alerta (administrador; zona opcional) |
| PUT | `/api/alertas/{id}?zonaRiesgoId={id}` | Actualizar alerta (administrador) |
| DELETE | `/api/alertas/{id}` | Eliminar alerta (administrador) |

Las alertas incluyen `severidad`: `BAJA`, `MEDIA`, `ALTA` o `CRITICA`. Si se omite al crear
una alerta, se asigna `MEDIA`.

Cuerpo JSON esperado en POST/PUT:
```json
{
  "titulo": "Alerta de seguridad",
  "mensaje": "Se reportan hurtos frecuentes en el sector",
  "tipo": "SEGURIDAD",
  "severidad": "ALTA",
  "activa": true
}
```

### Sembrado masivo de datos de demostración

Un administrador autenticado puede solicitar una sola vez la creación de 10 ciudadanos, 10 zonas
de riesgo de Bogotá, 30 reportes y 12 alertas:

| Método | Endpoint | Acceso |
|---|---|---|
| POST | `/api/admin/seed-masivo` | Solo administrador |

La ejecución es explícita (no se activa durante el arranque), se registra en la base de datos y se
rechazan posteriores intentos con `409 Conflict`, incluso si la aplicación corre en varias
instancias. Los registros y contraseñas aleatorias de las cuentas de demostración se insertan en
la base de datos; dichas contraseñas no se devuelven. Antes de invocarlo en producción, confirma
que deseas añadir esos datos permanentes a esa base.
El endpoint solo acepta `POST`; `GET` responde `405 Method Not Allowed` y nunca modifica datos.
Las rutas `/api/**` que no existen responden como errores JSON, sin pasar por el servidor de
archivos estáticos.

## Manejo de errores

Todas las respuestas de error siguen este formato JSON, con el código HTTP correspondiente (`400`, `401`, `403`, `404`, `409`, `500`):
```json
{
  "timestamp": "2026-08-19T10:00:00",
  "status": 404,
  "mensaje": "Usuario no encontrado con id 5"
}
```

## Control de versiones

Este proyecto se gestiona con Git, con commits organizados por módulo/etapa. Para subirlo a un
repositorio remoto:
```bash
git init
git add .
git commit -m "EV01: integración de módulos del proyecto Ciudad Segura"
git branch -M main
git remote add origin <URL_DE_TU_REPOSITORIO>
git push -u origin main
```
