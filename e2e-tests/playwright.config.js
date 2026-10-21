const { defineConfig, devices } = require("@playwright/test");
const chromiumExecutablePath = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH;

module.exports = defineConfig({
    testDir: "./tests",
    fullyParallel: false,
    reporter: "list",
    use: {
        baseURL: process.env.BASE_URL || "http://127.0.0.1:18081",
        trace: "retain-on-failure",
        ...(chromiumExecutablePath
            ? { launchOptions: { executablePath: chromiumExecutablePath } }
            : {}),
    },
    projects: [
        {
            name: "chromium",
            use: { ...devices["Desktop Chrome"] },
        },
    ],
});
