import { createHash, randomBytes } from "node:crypto";
import { and, eq, gt } from "drizzle-orm";
import { db } from "../db/index.js";
import { sesiones, usuarios } from "../db/schema.js";

const DURACION_SESION_MS = 7 * 24 * 60 * 60 * 1000;

type Usuario = typeof usuarios.$inferSelect;

// Equivalente a UsuarioResponse: nunca expone la contraseña
export const usuarioResponse = (u: Usuario) => ({
  id: u.id,
  nombre: u.nombre,
  email: u.email,
  telefono: u.telefono,
});

// Equivalente a ApiResponse
export const apiResponse = (status: number, exito: boolean, mensaje: string, extra: object = {}) =>
  Response.json({ exito, mensaje, ...extra }, { status });

export const leerJson = async (req: Request): Promise<Record<string, unknown>> => {
  try {
    const body = await req.json();
    return body && typeof body === "object" ? body : {};
  } catch {
    return {};
  }
};

export const texto = (v: unknown) => (typeof v === "string" ? v.trim() : "");

const hashToken = (token: string) => createHash("sha256").update(token).digest("hex");

export const crearSesion = async (usuarioId: number) => {
  const token = randomBytes(32).toString("base64url");
  await db.insert(sesiones).values({
    tokenHash: hashToken(token),
    usuarioId,
    expiraEn: new Date(Date.now() + DURACION_SESION_MS),
  });
  return token;
};

const leerToken = (req: Request) => {
  const auth = req.headers.get("authorization") ?? "";
  return auth.startsWith("Bearer ") ? auth.slice(7) : null;
};

export const cerrarSesion = async (req: Request) => {
  const token = leerToken(req);
  if (token) await db.delete(sesiones).where(eq(sesiones.tokenHash, hashToken(token)));
};

// Devuelve el usuario autenticado o null si el token no es válido
export const usuarioActual = async (req: Request): Promise<Usuario | null> => {
  const token = leerToken(req);
  if (!token) return null;
  const [fila] = await db
    .select({ usuario: usuarios })
    .from(sesiones)
    .innerJoin(usuarios, eq(sesiones.usuarioId, usuarios.id))
    .where(and(eq(sesiones.tokenHash, hashToken(token)), gt(sesiones.expiraEn, new Date())));
  return fila?.usuario ?? null;
};

export const noAutenticado = () => apiResponse(401, false, "Debes iniciar sesión para realizar esta acción");
