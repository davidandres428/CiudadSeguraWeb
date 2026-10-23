const { test, expect } = require("@playwright/test");

test("rutas limpias sirven las vistas y las URLs HTML redirigen", async ({ page, request }) => {
    await page.goto("/");
    await expect(page).toHaveURL(/\/login$/);
    await expect(page).toHaveTitle("Iniciar sesión | Ciudad Segura");

    await page.goto("/inicio");
    await expect(page).toHaveTitle("Ciudad Segura");

    await page.goto("/index.html?origen=e2e");
    await expect(page).toHaveURL(/\/inicio\?origen=e2e$/);

    await page.goto("/reportes");
    await expect(page).toHaveURL(/\/reportes$/);
    await expect(page).toHaveTitle("Ciudad Segura — Reportes");

    await page.goto("/reportes.html?origen=e2e");
    await expect(page).toHaveURL(/\/reportes\?origen=e2e$/);

    const apiResponse = await request.get("/api/reportes");
    expect(apiResponse.status()).toBe(200);
    const assetResponse = await request.get("/js/api.js");
    expect(assetResponse.status()).toBe(200);
});

test("la portada muestra noticias desde la API como contenido de texto", async ({ page }) => {
    await page.route("**/api/noticias", (route) => route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify([{
            titulo: "<img src=x onerror=alert(1)> Comunicado comunitario",
            categoria: "Comunidad",
            contenido: "Jornada informativa de prevención ciudadana.",
            fechaPublicacion: "2026-10-20T09:30:00",
        }]),
    }));

    await page.goto("/inicio");

    const comunicado = page.locator("#lista-comunicados .comunicado");
    await expect(comunicado).toHaveCount(1);
    await expect(comunicado).toContainText("Comunicado comunitario");
    await expect(comunicado.locator("img")).toHaveCount(0);
    await expect(comunicado.locator("time")).toContainText("20 de octubre de 2026");
});

test("login adaptable ofrece acceso, recordatorio y ayuda de contraseña", async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await page.goto("/login");

    await expect(page).toHaveTitle("Iniciar sesión | Ciudad Segura");
    await expect(page.getByRole("heading", { name: "Inicia sesión" })).toBeVisible();
    await expect(page.locator("#login-email")).toBeVisible();
    await expect(page.locator("#login-password")).toBeVisible();
    await expect(page.getByRole("checkbox", { name: "Recordarme" })).toBeVisible();
    await expect(page.getByRole("button", { name: "Iniciar sesión" })).toBeVisible();
    await expect(page.getByRole("button", { name: "Instagram no disponible" })).toBeDisabled();
    await page.getByRole("link", { name: "¿Olvidaste tu contraseña?" }).click();
    await expect(page.locator("#forgot-help")).toBeVisible();

    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
    expect(overflow).toBe(false);

    await page.getByRole("tab", { name: "Crear cuenta" }).click();
    await expect(page.getByRole("heading", { name: "Crea tu cuenta" })).toBeVisible();
    await page.getByRole("tab", { name: "Iniciar sesión" }).click();
    await expect(page.getByRole("heading", { name: "Inicia sesión" })).toBeVisible();
});

test("el enlace de gestión de usuarios solo se muestra a administradores", async ({ page }) => {
    await page.goto("/inicio");
    const enlaceUsuarios = page.locator('nav.menu a[href="/usuarios"]');
    await expect(enlaceUsuarios).toBeHidden();

    await page.evaluate(() => {
        sessionStorage.setItem("cs_usuario", JSON.stringify({ nombre: "Admin", rol: "ROLE_ADMIN" }));
        sessionStorage.setItem("cs_access_token", "token-ui-test");
    });
    await page.reload();
    await expect(enlaceUsuarios).toBeVisible();

    await page.evaluate(() => {
        sessionStorage.setItem("cs_usuario", JSON.stringify({ nombre: "Ciudadano", rol: "CIUDADANO" }));
    });
    await page.reload();
    await expect(enlaceUsuarios).toBeHidden();
});

test("registro ciudadano, acceso, creación de reporte y métricas en vivo", async ({ page }) => {
    await page.goto("/inicio");
    const fechaHoy = new Date();
    const fechaLocal = (fecha) => [
        fecha.getFullYear(),
        String(fecha.getMonth() + 1).padStart(2, "0"),
        String(fecha.getDate()).padStart(2, "0"),
    ].join("-");
    const parametrosConteo = new URLSearchParams({
        desde: `${fechaHoy.getFullYear()}-${String(fechaHoy.getMonth() + 1).padStart(2, "0")}-01`,
        hasta: fechaLocal(fechaHoy),
    });
    const respuestaConteo = await page.request.get(
        new URL(`/api/reportes/count?${parametrosConteo}`, page.url()).toString(),
    );
    expect(respuestaConteo.ok()).toBeTruthy();
    const reportesAntes = Number(await respuestaConteo.text());
    const tituloReporte = `Incidente navegador ${Date.now()}`;
    await page.getByRole("button", { name: /radicar una pqrs/i }).click();
    await page.locator("#pqrs-nombre").fill("Ciudadana E2E");
    await page.locator("#pqrs-email").fill(`pqrs-${Date.now()}@example.com`);
    await page.locator("#pqrs-asunto").fill("Solicitud de prueba E2E");
    await page.locator("#pqrs-detalle").fill("Validación de radicación local.");
    await page.getByRole("button", { name: "Enviar solicitud" }).click();
    await expect(page.locator("#confirmacion-pqrs")).toContainText("Solicitud radicada");

    await page.goto("/reportes");
    await expect(page.locator("#form-reporte")).toBeHidden();
    await page.getByRole("link", { name: "iniciar sesión", exact: true }).click();
    await expect(page).toHaveURL(/\/login#form-login$/);

    await page.getByRole("tab", { name: "Crear cuenta" }).click();
    await expect(page.locator("#panel-registro")).toBeVisible();
    await page.locator("#reg-nombre").fill("Prueba navegador");
    await page.locator("#reg-email").fill(`e2e-${Date.now()}@example.com`);
    await page.locator("#reg-password").fill("ClaveSegura123");
    await page.getByRole("button", { name: "Crear cuenta" }).click();
    await expect(page).toHaveURL(/\/reportes$/);
    await expect(page.locator("#usuario-activo")).toContainText("Prueba navegador");

    await page.goto("/reportes");
    await expect(page.locator("#form-reporte")).toBeVisible();
    await page.locator("#reporte-titulo").fill(tituloReporte);
    await page.locator("#reporte-descripcion").fill("Prueba de persistencia visual");
    await page.locator("#reporte-direccion").fill("Carrera 10");
    await page.locator("#reporte-tipo").selectOption("ROBO");
    await page.locator("#reporte-prioridad").selectOption("MEDIA");
    await expect.poll(() => page.evaluate(() => Boolean(window.L))).toBe(true);
    await page.locator("#mapa-reportes").click({ position: { x: 100, y: 100 } });
    await expect(page.locator("#reporte-lat")).not.toHaveValue("");
    await expect(page.locator("#reporte-lng")).not.toHaveValue("");
    await page.getByRole("button", { name: "Registrar reporte" }).click();
    await expect(page.locator("#tabla-reportes")).toContainText(tituloReporte);
    await expect.poll(async () => page.locator("#mapa-reportes .leaflet-marker-icon").count())
        .toBeGreaterThan(0);

    await page.goto("/zonas");
    await expect(page.locator("#form-zona")).toBeVisible();
    await expect(page.locator("#aviso-acceso-zona")).toBeHidden();
    const nombreZona = `Zona ciudadana ${Date.now()}`;
    await page.locator("#zona-nombre").fill(nombreZona);
    await page.locator("#zona-descripcion").fill("Zona registrada por ciudadanía.");
    await page.locator("#zona-nivel").selectOption("MEDIO");
    await page.locator("#zona-lat").fill("4.61");
    await page.locator("#zona-lng").fill("-74.08");
    await page.getByRole("button", { name: "Registrar zona" }).click();
    await expect(page.locator("#tabla-zonas")).toContainText(nombreZona);
    await expect.poll(async () => page.locator("#mapa-zonas .leaflet-marker-icon").count())
        .toBeGreaterThan(0);

    await page.goto("/inicio");
    await expect(page.locator("#metrica-reportes")).toHaveText(String(reportesAntes + 1));
    await page.getByRole("button", { name: "Salir" }).click();
    await expect(page).toHaveURL(/\/login$/);
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login#form-login$/);
});
