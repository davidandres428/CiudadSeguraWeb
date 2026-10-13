// Cliente de la API de Ciudad Segura (Netlify Functions en /api/*)
const CLAVE_SESION = "ciudadsegura.sesion";
const ETIQUETAS_ESTADO = { pendiente: "Pendiente", en_revision: "En revisión", resuelto: "Resuelto" };

let sesion = JSON.parse(localStorage.getItem(CLAVE_SESION) || "null");
let reportes = [];

const $ = (sel, raiz = document) => raiz.querySelector(sel);

async function api(ruta, opciones = {}) {
  const headers = { "Content-Type": "application/json", ...(opciones.headers || {}) };
  if (sesion?.token) headers.Authorization = `Bearer ${sesion.token}`;
  const res = await fetch(ruta, { ...opciones, headers });
  const datos = res.status === 204 ? null : await res.json().catch(() => null);
  if (res.status === 401 && sesion && ruta !== "/api/auth/login") guardarSesion(null);
  if (!res.ok) throw new Error(datos?.mensaje || `Error ${res.status}`);
  return datos;
}

function guardarSesion(nueva) {
  sesion = nueva;
  if (nueva) localStorage.setItem(CLAVE_SESION, JSON.stringify(nueva));
  else localStorage.removeItem(CLAVE_SESION);
  pintarSesion();
  pintarReportes();
}

function avisar(texto) {
  const aviso = $("#aviso");
  aviso.textContent = texto;
  aviso.classList.add("visible");
  clearTimeout(avisar.t);
  avisar.t = setTimeout(() => aviso.classList.remove("visible"), 3000);
}

function escapar(texto) {
  const div = document.createElement("div");
  div.textContent = texto ?? "";
  return div.innerHTML;
}

function pintarSesion() {
  const nav = $("#sesion");
  if (sesion) {
    nav.innerHTML = `<span class="saludo">Hola, ${escapar(sesion.usuario.nombre.split(" ")[0])}</span>
      <button class="btn btn-fantasma" id="btn-salir">Cerrar sesión</button>`;
    $("#btn-salir").addEventListener("click", async () => {
      await api("/api/auth/logout", { method: "POST" }).catch(() => {});
      guardarSesion(null);
      avisar("Sesión cerrada");
    });
  } else {
    nav.innerHTML = `<button class="btn btn-fantasma" data-abrir="login">Iniciar sesión</button>
      <button class="btn btn-primario" data-abrir="registro">Crear cuenta</button>`;
  }
}

function pintarContadores(lista) {
  const cuenta = { total: lista.length, pendiente: 0, en_revision: 0, resuelto: 0 };
  lista.forEach((r) => (cuenta[r.estado] = (cuenta[r.estado] || 0) + 1));
  document.querySelectorAll("[data-cont]").forEach((el) => (el.textContent = cuenta[el.dataset.cont] ?? 0));
}

function pintarReportes() {
  const cat = $("#filtro-categoria").value;
  const est = $("#filtro-estado").value;
  const visibles = reportes.filter((r) => (!cat || r.categoria === cat) && (!est || r.estado === est));
  const lista = $("#lista");

  if (!visibles.length) {
    lista.innerHTML = `<div class="vacio">${reportes.length ? "No hay reportes con esos filtros." : "Aún no hay reportes. ¡Sé el primero en reportar!"}</div>`;
    return;
  }

  const fmt = new Intl.DateTimeFormat("es", { dateStyle: "medium", timeStyle: "short" });
  lista.innerHTML = visibles.map((r) => {
    const propio = sesion && sesion.usuario.id === r.usuarioId;
    return `<article class="tarjeta">
      <div class="tarjeta-meta">
        <span class="chip" data-cat="${escapar(r.categoria)}">${escapar(r.categoria)}</span>
        <span class="estado" data-estado="${escapar(r.estado)}">${ETIQUETAS_ESTADO[r.estado] || escapar(r.estado)}</span>
      </div>
      <h3>${escapar(r.titulo)}</h3>
      <p class="ubicacion">${escapar(r.ubicacion)}</p>
      <p>${escapar(r.descripcion)}</p>
      <div class="tarjeta-pie">
        <span>${escapar(r.autor)} · ${fmt.format(new Date(r.fechaReporte))}</span>
        ${propio ? `<span class="tarjeta-acciones">
          <button class="btn btn-fantasma btn-mini" data-editar="${r.id}">Editar</button>
          <button class="btn btn-peligro btn-mini" data-eliminar="${r.id}">Eliminar</button>
        </span>` : ""}
      </div>
    </article>`;
  }).join("");
}

async function cargarReportes() {
  try {
    reportes = await api("/api/reportes");
    pintarContadores(reportes);
    pintarReportes();
  } catch (e) {
    $("#lista").innerHTML = `<div class="vacio">No se pudieron cargar los reportes: ${escapar(e.message)}</div>`;
  }
}

function abrir(nombre) {
  document.querySelectorAll("dialog[open]").forEach((d) => d.close());
  const dlg = $(`#dlg-${nombre}`);
  const form = $("form", dlg);
  $(".error", form).textContent = "";
  dlg.showModal();
}

function abrirReporte(reporte) {
  if (!sesion) {
    avisar("Inicia sesión para crear reportes");
    return abrir("login");
  }
  const form = $("#form-reporte");
  form.reset();
  form.classList.toggle("editando", Boolean(reporte));
  $("[data-titulo]", form).textContent = reporte ? "Editar reporte" : "Nuevo reporte";
  if (reporte) {
    for (const campo of ["id", "titulo", "categoria", "ubicacion", "descripcion", "estado"]) form.elements[campo].value = reporte[campo];
  } else {
    form.elements.id.value = "";
  }
  abrir("reporte");
}

function enviarForm(form, accion) {
  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const boton = $("[type=submit]", form);
    const error = $(".error", form);
    error.textContent = "";
    boton.disabled = true;
    try {
      await accion(Object.fromEntries(new FormData(form)));
      form.closest("dialog").close();
      form.reset();
    } catch (e) {
      error.textContent = e.message;
    } finally {
      boton.disabled = false;
    }
  });
}

enviarForm($("#form-login"), async (datos) => {
  const r = await api("/api/auth/login", { method: "POST", body: JSON.stringify(datos) });
  guardarSesion({ token: r.token, usuario: r.usuario });
  avisar(`Bienvenido, ${r.usuario.nombre}`);
});

enviarForm($("#form-registro"), async (datos) => {
  const r = await api("/api/auth/registro", { method: "POST", body: JSON.stringify(datos) });
  guardarSesion({ token: r.token, usuario: r.usuario });
  avisar(r.mensaje);
});

enviarForm($("#form-reporte"), async ({ id, ...datos }) => {
  if (id) await api(`/api/reportes/${id}`, { method: "PUT", body: JSON.stringify(datos) });
  else await api("/api/reportes", { method: "POST", body: JSON.stringify(datos) });
  avisar(id ? "Reporte actualizado" : "Reporte publicado");
  await cargarReportes();
});

document.addEventListener("click", async (ev) => {
  const el = ev.target.closest("[data-abrir],[data-cerrar],[data-editar],[data-eliminar]");
  if (!el) return;
  ev.preventDefault();
  if (el.dataset.abrir) abrir(el.dataset.abrir);
  else if ("cerrar" in el.dataset) el.closest("dialog").close();
  else if (el.dataset.editar) abrirReporte(reportes.find((r) => r.id === Number(el.dataset.editar)));
  else if (el.dataset.eliminar && confirm("¿Eliminar este reporte?")) {
    try {
      await api(`/api/reportes/${el.dataset.eliminar}`, { method: "DELETE" });
      avisar("Reporte eliminado");
      await cargarReportes();
    } catch (e) {
      avisar(e.message);
    }
  }
});

$("#btn-nuevo").addEventListener("click", () => abrirReporte(null));
$("#filtro-categoria").addEventListener("change", pintarReportes);
$("#filtro-estado").addEventListener("change", pintarReportes);

fetch("/api/hola")
  .then((r) => (r.ok ? r.text() : Promise.reject()))
  .then((t) => ($("#estado-api").textContent = `● ${t}`))
  .catch(() => ($("#estado-api").textContent = "○ Servidor no disponible"));

// Valida que la sesión guardada siga activa
if (sesion) api("/api/auth/yo").then((r) => guardarSesion({ ...sesion, usuario: r.usuario })).catch(() => {});

pintarSesion();
cargarReportes();
