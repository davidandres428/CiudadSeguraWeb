const { test, expect } = require("@playwright/test");

test("registro ciudadano, acceso, creación de reporte y métricas en vivo", async ({ page }) => {
    await page.goto("/reportes.html");
    await expect(page.locator("#form-reporte")).toBeHidden();
    await page.getByRole("link", { name: "iniciar sesión" }).click();
    await expect(page).toHaveURL(/\/login\.html#form-login$/);

    await page.getByRole("link", { name: "Registrarse" }).click();
    await expect(page.locator("#panel-registro")).toBeVisible();
    await page.locator("#reg-nombre").fill("Prueba navegador");
    await page.locator("#reg-email").fill(`e2e-${Date.now()}@example.com`);
    await page.locator("#reg-password").fill("ClaveSegura123");
    await page.getByRole("button", { name: "Crear cuenta" }).click();
    await expect(page).toHaveURL(/\/index\.html$/);
    await expect(page.locator("#usuario-activo")).toContainText("Prueba navegador");

    await page.goto("/reportes.html");
    await expect(page.locator("#form-reporte")).toBeVisible();
    await page.locator("#reporte-titulo").fill("Incidente desde navegador");
    await page.locator("#reporte-descripcion").fill("Prueba de persistencia visual");
    await page.locator("#reporte-direccion").fill("Carrera 10");
    await page.locator("#reporte-tipo").selectOption("ROBO");
    await page.locator("#reporte-prioridad").selectOption("MEDIA");
    await page.getByRole("button", { name: "Registrar reporte" }).click();
    await expect(page.locator("#tabla-reportes")).toContainText("Incidente desde navegador");

    await page.goto("/zonas.html");
    await expect(page.locator("#form-zona")).toBeHidden();
    await expect(page.locator("#aviso-acceso-zona")).toContainText("reservada para administradores");

    await page.goto("/index.html");
    await expect(page.locator("#metrica-reportes")).toHaveText("1");
});
