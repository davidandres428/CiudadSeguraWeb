/**
 * api.js
 * Módulo compartido por todas las páginas del front-end de Ciudad Segura.
 * Centraliza las llamadas a la API REST (back-end Spring Boot) y el manejo
 * de la sesión del usuario autenticado (guardada en memoria + sessionStorage).
 */

const API_BASE = `${(window.CIUDAD_SEGURA_API_URL || "").replace(/\/$/, "")}/api`;

/** Realiza una petición fetch y normaliza el manejo de errores del back-end. */
async function apiFetch(path, options = {}) {
    const respuesta = await fetch(API_BASE + path, {
        ...options,
        headers: { "Content-Type": "application/json", ...Sesion.cabeceraAuth(), ...(options.headers || {}) },
    });

    if (respuesta.status === 401 && !path.startsWith("/auth/")) {
        Sesion.cerrar();
        throw new Error("Tu sesión expiró. Inicia sesión de nuevo.");
    }

    if (respuesta.status === 204) {
        return null;
    }

    const texto = await respuesta.text();
    const datos = texto ? JSON.parse(texto) : null;

    if (!respuesta.ok) {
        const mensaje = datos && (datos.mensaje || datos.detalles || datos.error)
            ? (datos.mensaje || datos.detalles || datos.error)
            : `Error ${respuesta.status}`;
        throw new Error(mensaje);
    }
    return datos;
}

const CiudadSeguraAPI = {
    // --- Autenticación / Usuarios ---
    registrar: (datos) => apiFetch("/auth/registro", { method: "POST", body: JSON.stringify(datos) }),
    login: (datos) => apiFetch("/auth/login", { method: "POST", body: JSON.stringify(datos) }),
    listarUsuarios: () => apiFetch("/usuarios"),
    obtenerUsuario: (id) => apiFetch(`/usuarios/${id}`),
    actualizarUsuario: (id, datos) => apiFetch(`/usuarios/${id}`, { method: "PUT", body: JSON.stringify(datos) }),
    eliminarUsuario: (id) => apiFetch(`/usuarios/${id}`, { method: "DELETE" }),

    // --- Reportes de incidentes ---
    listarReportes: (params = "") => apiFetch(`/reportes${params}`),
    obtenerReporte: (id) => apiFetch(`/reportes/${id}`),
    crearReporte: (datos) => apiFetch("/reportes", { method: "POST", body: JSON.stringify(datos) }),
    actualizarReporte: (id, datos) => apiFetch(`/reportes/${id}`, { method: "PUT", body: JSON.stringify(datos) }),
    eliminarReporte: (id) => apiFetch(`/reportes/${id}`, { method: "DELETE" }),

    // --- Zonas de riesgo ---
    listarZonas: (params = "") => apiFetch(`/zonas-riesgo${params}`),
    obtenerZona: (id) => apiFetch(`/zonas-riesgo/${id}`),
    crearZona: (datos) => apiFetch("/zonas-riesgo", { method: "POST", body: JSON.stringify(datos) }),
    actualizarZona: (id, datos) => apiFetch(`/zonas-riesgo/${id}`, { method: "PUT", body: JSON.stringify(datos) }),
    eliminarZona: (id) => apiFetch(`/zonas-riesgo/${id}`, { method: "DELETE" }),

    // --- Alertas ---
    listarAlertas: (params = "") => apiFetch(`/alertas${params}`),
    obtenerAlerta: (id) => apiFetch(`/alertas/${id}`),
    crearAlerta: (datos, zonaRiesgoId) => apiFetch(`/alertas${zonaRiesgoId ? "?zonaRiesgoId=" + zonaRiesgoId : ""}`, { method: "POST", body: JSON.stringify(datos) }),
    actualizarAlerta: (id, datos, zonaRiesgoId) => apiFetch(`/alertas/${id}${zonaRiesgoId ? "?zonaRiesgoId=" + zonaRiesgoId : ""}`, { method: "PUT", body: JSON.stringify(datos) }),
    eliminarAlerta: (id) => apiFetch(`/alertas/${id}`, { method: "DELETE" }),
};

// --- Sesión del ciudadano autenticado ---
const Sesion = {
    /** Guarda la respuesta de login/registro: { token, usuario }. */
    guardar(auth) {
        sessionStorage.setItem("cs_usuario", JSON.stringify(auth.usuario));
        sessionStorage.setItem("cs_token", auth.token);
    },
    token() {
        return sessionStorage.getItem("cs_token");
    },
    cabeceraAuth() {
        const t = Sesion.token();
        return t ? { Authorization: `Bearer ${t}` } : {};
    },
    esAdmin() {
        const u = Sesion.obtener();
        return !!u && u.rol === "ADMINISTRADOR";
    },
    obtener() {
        const datos = sessionStorage.getItem("cs_usuario");
        return datos ? JSON.parse(datos) : null;
    },
    cerrar() {
        sessionStorage.removeItem("cs_usuario");
        sessionStorage.removeItem("cs_token");
        window.location.href = "/login.html";
    },
};

/** Pinta la barra de usuario activo en la esquina de la topbar, si hay sesión. */
function pintarUsuarioActivo() {
    const contenedor = document.getElementById("usuario-activo");
    if (!contenedor) return;
    const usuario = Sesion.obtener();
    if (usuario) {
        contenedor.innerHTML = `
            <span class="usuario-activo">
                👤 ${usuario.nombre}
                <button class="secundario" onclick="Sesion.cerrar()">Salir</button>
            </span>`;
    } else {
        contenedor.innerHTML = `<a href="/login.html" class="boton">Iniciar sesión</a>`;
    }
}

function mostrarMensaje(contenedorId, texto, tipo = "error") {
    const el = document.getElementById(contenedorId);
    if (!el) return;
    el.textContent = texto;
    el.className = `mensaje ${tipo}`;
    el.classList.remove("oculto");
}

function ocultarMensaje(contenedorId) {
    const el = document.getElementById(contenedorId);
    if (el) el.classList.add("oculto");
}

function formatearFecha(iso) {
    if (!iso) return "-";
    const f = new Date(iso);
    return f.toLocaleString("es-CO");
}

/** Exige sesión en todas las páginas salvo el login y oculta lo reservado a administradores. */
function protegerPagina() {
    if (!Sesion.token() && !location.pathname.endsWith("/login.html")) {
        window.location.href = "/login.html";
        return;
    }
    if (!Sesion.esAdmin()) document.body.classList.add("no-admin");
}

document.addEventListener("DOMContentLoaded", () => {
    protegerPagina();
    pintarUsuarioActivo();
});
