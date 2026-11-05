document.addEventListener("DOMContentLoaded", cargarUsuarios);

async function cargarUsuarios() {
    const cuerpo = document.getElementById("tabla-usuarios-body");
    const mensaje = document.getElementById("mensaje-usuarios");
    const sesion = Sesion.obtener();

    if (!Sesion.token() || !sesion) {
        window.location.href = "/login#form-login";
        return;
    }

    if (sesion.rol !== "ADMINISTRADOR" && sesion.rol !== "ROLE_ADMIN") {
        mensaje.textContent = "Solo un administrador puede gestionar los roles de usuario.";
        return;
    }

    try {
        const usuarios = await CiudadSeguraAPI.listarUsuarios();
        cuerpo.innerHTML = usuarios.map((usuario) => {
            const esAdminPrincipal = usuario.email?.toLowerCase() === "david.andresgalvis@gmail.com";
            const rol = usuario.rol === "ADMINISTRADOR" ? "ADMINISTRADOR" : "CIUDADANO";
            const controlRol = esAdminPrincipal
                ? "Administrador principal"
                : `<label for="rol-usuario-${usuario.id}">Rol</label>
                    <select id="rol-usuario-${usuario.id}" data-rol-usuario="${usuario.id}"
                            aria-label="Rol para ${escaparHtml(usuario.nombre)}">
                        <option value="CIUDADANO" ${rol === "CIUDADANO" ? "selected" : ""}>Ciudadano</option>
                        <option value="ADMINISTRADOR" ${rol === "ADMINISTRADOR" ? "selected" : ""}>Administrador</option>
                    </select>
                    <button type="button" data-guardar-rol="${usuario.id}">Guardar rol</button>`;
            return `<tr>
                <td>${escaparHtml(usuario.id)}</td>
                <td>${escaparHtml(usuario.nombre)}</td>
                <td>${escaparHtml(usuario.email)}</td>
                <td>${controlRol}</td>
            </tr>`;
        }).join("");
        cuerpo.addEventListener("click", guardarRolSeleccionado);
    } catch (error) {
        mensaje.textContent = error.message || "No fue posible cargar los usuarios.";
        if (error.status === 401) {
            window.location.href = "/login#form-login";
        }
    }
}

async function guardarRolSeleccionado(event) {
    const boton = event.target.closest("[data-guardar-rol]");
    if (!boton) return;

    const id = boton.dataset.guardarRol;
    const selector = document.querySelector(`[data-rol-usuario="${id}"]`);
    const mensaje = document.getElementById("mensaje-usuarios");
    boton.disabled = true;
    mensaje.textContent = "";

    try {
        await CiudadSeguraAPI.cambiarRolUsuario(id, selector.value);
        mensaje.textContent = "Rol actualizado correctamente.";
    } catch (error) {
        mensaje.textContent = error.message || "No fue posible actualizar el rol.";
    } finally {
        boton.disabled = false;
    }
}
