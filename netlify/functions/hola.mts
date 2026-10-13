import type { Config } from "@netlify/functions";

export default async () => new Response("¡Servidor de Ciudad Segura funcionando correctamente!");

export const config: Config = { path: "/api/hola", method: "GET" };
