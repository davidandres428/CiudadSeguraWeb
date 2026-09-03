# Ciudad Segura — Aplicación integrada (back-end + front-end + base de datos)

Proyecto formativo: **Construcción de software integrador de tecnologías orientadas a servicios.**
Evidencia: **GA8-220501096-AA1-EV01** — Integrar módulos, de acuerdo con el diseño establecido.

Este proyecto integra en **una sola aplicación Spring Boot** los módulos construidos en guías
anteriores: un menú principal (front-end) que conecta con los servicios REST (back-end) y estos,
a su vez, con la base de datos (MySQL), de modo que el CRUD completo de cada módulo se puede
ejecutar desde una interfaz real en el navegador.

## Módulos integrados

1. **Usuarios**: registro e inicio de sesión de ciudadanos (contraseña cifrada con BCrypt), y CRUD de usuarios.
2. **Reportes de incidentes**: los ciudadanos reportan hechos de inseguridad (robo, vandalismo, accidente, disturbio) en una ubicación geográfica.
3. **Zonas de riesgo**: zonas geográficas clasificadas por nivel de riesgo (bajo, medio, alto).
4. **Alertas**: notificaciones emitidas a los ciudadanos, opcionalmente asociadas a una zona de riesgo.

El **menú principal** (`index.html`) enlaza los cuatro módulos y cada página consume los mismos
endpoints REST documentados abajo, verificando que la navegación y la conexión a base de datos
funcionen de extremo a extremo.

## Tecnologías

- Java 17 · Spring Boot 3.2.3
- Spring Web (API REST) + front-end estático (HTML, CSS y JavaScript puro) servido desde `src/main/resources/static`
- Spring Data JPA + MySQL
- Spring Validation (`@NotBlank`, `@Email`, etc.)
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
    ├── css/styles.css
    └── js/api.js         -> cliente REST compartido por todas las páginas

src/test/java/com/ciudadsegura/service/  -> pruebas unitarias (JUnit 5 + Mockito)
```

## Configuración y ejecución

1. Tener MySQL corriendo localmente (o ajustar la URL a tu servidor).
2. Ajustar usuario/contraseña de MySQL en `src/main/resources/application.properties`
   (por defecto: `root` / `root`, base de datos `ciudad_segura`, se crea automáticamente).
3. Ejecutar:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Abrir el navegador en `http://localhost:8080` — ahí se carga el **menú principal** con acceso
   a los cuatro módulos.
5. Para registrarte como ciudadano y poder crear reportes, entra a `http://localhost:8080/login.html`.

## Ejecutar las pruebas unitarias

```bash
./mvnw test
```

## Endpoints

### Autenticación — `/api/auth`
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/registro` | Registra un nuevo usuario (nombre, email, password) |
| POST | `/api/auth/login` | Valida credenciales y retorna los datos del usuario |

### Usuarios — `/api/usuarios`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Consultar todos los usuarios |
| GET | `/api/usuarios/{id}` | Consultar un usuario por id |
| PUT | `/api/usuarios/{id}` | Actualizar un usuario (nombre, email) |
| DELETE | `/api/usuarios/{id}` | Eliminar un usuario |

### Reportes de incidentes — `/api/reportes`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/reportes` | Consultar todos los reportes |
| GET | `/api/reportes?usuarioId={id}` | Consultar reportes de un usuario |
| GET | `/api/reportes?estado={PENDIENTE\|EN_REVISION\|RESUELTO\|DESCARTADO}` | Filtrar por estado |
| GET | `/api/reportes/{id}` | Consultar un reporte por id |
| POST | `/api/reportes?usuarioId={id}` | Registrar un nuevo reporte |
| PUT | `/api/reportes/{id}` | Actualizar un reporte |
| DELETE | `/api/reportes/{id}` | Eliminar un reporte |

Cuerpo JSON esperado en POST/PUT:
```json
{
  "titulo": "Robo en la calle 45",
  "descripcion": "Robo a mano armada frente al parque",
  "tipo": "ROBO",
  "latitud": 4.710989,
  "longitud": -74.072092
}
```

### Zonas de riesgo — `/api/zonas-riesgo`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/zonas-riesgo` | Consultar todas las zonas |
| GET | `/api/zonas-riesgo?nivel={BAJO\|MEDIO\|ALTO}` | Filtrar por nivel de riesgo |
| GET | `/api/zonas-riesgo/{id}` | Consultar una zona por id |
| POST | `/api/zonas-riesgo` | Registrar una nueva zona |
| PUT | `/api/zonas-riesgo/{id}` | Actualizar una zona |
| DELETE | `/api/zonas-riesgo/{id}` | Eliminar una zona |

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
| POST | `/api/alertas?zonaRiesgoId={id}` | Registrar alerta (zona opcional) |
| PUT | `/api/alertas/{id}?zonaRiesgoId={id}` | Actualizar alerta |
| DELETE | `/api/alertas/{id}` | Eliminar alerta |

Cuerpo JSON esperado en POST/PUT:
```json
{
  "titulo": "Alerta de seguridad",
  "mensaje": "Se reportan hurtos frecuentes en el sector",
  "tipo": "SEGURIDAD",
  "activa": true
}
```

## Manejo de errores

Todas las respuestas de error siguen este formato JSON, con el código HTTP correspondiente (`400`, `401`, `404`, `409`, `500`):
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
