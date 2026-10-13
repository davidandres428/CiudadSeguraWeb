import type { Config, Context } from "@netlify/functions";
import { eq } from "drizzle-orm";
import { db } from "../../db/index.js";
import { usuarios } from "../../db/schema.js";
import { apiResponse, noAutenticado, usuarioActual, usuarioResponse } from "../../lib/http.js";

// Equivalente a UsuarioController; requiere sesión porque expone datos personales
export default async (req: Request, context: Context) => {
  const actual = await usuarioActual(req);
  if (!actual) return noAutenticado();

  const id = context.params.id ? Number(context.params.id) : null;

  if (req.method === "GET" && id === null) {
    const lista = await db.select().from(usuarios).orderBy(usuarios.id);
    return Response.json(lista.map(usuarioResponse));
  }

  if (id === null || !Number.isInteger(id)) return apiResponse(404, false, "Usuario no encontrado");

  if (req.method === "GET") {
    const [usuario] = await db.select().from(usuarios).where(eq(usuarios.id, id));
    return usuario ? Response.json(usuarioResponse(usuario)) : apiResponse(404, false, "Usuario no encontrado");
  }

  // Un usuario solo puede eliminar su propia cuenta
  if (req.method === "DELETE") {
    if (id !== actual.id) return apiResponse(403, false, "Solo puedes eliminar tu propia cuenta");
    await db.delete(usuarios).where(eq(usuarios.id, id));
    return new Response(null, { status: 204 });
  }

  return apiResponse(405, false, "Método no permitido");
};

export const config: Config = { path: ["/api/usuarios", "/api/usuarios/:id"] };
