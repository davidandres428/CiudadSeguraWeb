const { test, expect } = require("@playwright/test");

test("rutas limpias sirven las vistas y las URLs HTML redirigen", async ({ page, request }) => {
    await page.goto("/");
    await expect(page).toHaveURL(/\/$/);
    await expect(page).toHaveTitle("Ciudad Segura");

    await page.goto("/index.html?origen=e2e");
    await expect(page).toHaveURL(/\/\?origen=e2e$/);

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

test("registro ciudadano, acceso, creación de reporte y métricas en vivo", async ({ page }) => {
    await page.goto("/");
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

    await page.getByRole("link", { name: "Registrarse" }).click();
    await expect(page.locator("#panel-registro")).toBeVisible();
    await page.locator("#reg-nombre").fill("Prueba navegador");
    await page.locator("#reg-email").fill(`e2e-${Date.now()}@example.com`);
    await page.locator("#reg-password").fill("ClaveSegura123");
    await page.getByRole("button", { name: "Crear cuenta" }).click();
    await expect(page).toHaveURL(/\/$/);
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
    await expect(page.locator("#form-zona")).toBeHidden();
    await expect(page.locator("#aviso-acceso-zona")).toContainText("reservada para administradores");
    await expect.poll(async () => page.locator("#mapa-zonas .leaflet-marker-icon").count())
        .toBeGreaterThan(0);

    await page.goto("/");
    await expect(page.locator("#metrica-reportes")).toHaveText(String(reportesAntes + 1));
    await page.getByRole("button", { name: "Salir" }).click();
    await expect(page).toHaveURL(/\/login$/);
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login#form-login$/);
});
