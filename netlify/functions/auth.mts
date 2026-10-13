import type { Config, Context } from "@netlify/functions";
import bcrypt from "bcryptjs";
import { eq } from "drizzle-orm";
import { db } from "../../db/index.js";
import { usuarios } from "../../db/schema.js";
import {
  apiResponse, cerrarSesion, crearSesion, leerJson, noAutenticado, texto, usuarioActual, usuarioResponse,
} from "../../lib/http.js";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// POST /api/auth/registro -> equivalente a AuthService.registrar
const registrar = async (req: Request) => {
  const body = await leerJson(req);
  const nombre = texto(body.nombre);
  const email = texto(body.email).toLowerCase();
  const password = typeof body.password === "string" ? body.password : "";
  const telefono = texto(body.telefono) || null;

  const errores: string[] = [];
  if (!nombre) errores.push("El nombre es obligatorio");
  if (!email) errores.push("El correo electrónico es obligatorio");
  else if (!EMAIL_RE.test(email)) errores.push("El correo electrónico no tiene un formato válido");
  if (!password) errores.push("La contraseña es obligatoria");
  else if (password.length < 6) errores.push("La contraseña debe tener al menos 6 caracteres");
  if (nombre.length > 100 || email.length > 100 || (telefono?.length ?? 0) > 20)
    errores.push("Alguno de los campos excede la longitud permitida");
  if (errores.length) return apiResponse(400, false, errores.join(". "));

  const [existente] = await db.select({ id: usuarios.id }).from(usuarios).where(eq(usuarios.email, email));
  if (existente) return apiResponse(409, false, `Ya existe una cuenta registrada con el correo ${email}`);

  const [guardado] = await db
    .insert(usuarios)
    .values({ nombre, email, telefono, password: await bcrypt.hash(password, 10) })
    .returning();
  const token = await crearSesion(guardado.id);
  return apiResponse(200, true, "Usuario registrado exitosamente", { usuario: usuarioResponse(guardado), token });
};

// POST /api/auth/login -> equivalente a AuthService.iniciarSesion
const iniciarSesion = async (req: Request) => {
  const body = await leerJson(req);
  const email = texto(body.email).toLowerCase();
  const password = typeof body.password === "string" ? body.password : "";

  const errores: string[] = [];
  if (!email) errores.push("El correo electrónico es obligatorio");
  else if (!EMAIL_RE.test(email)) errores.push("El correo electrónico no tiene un formato válido");
  if (!password) errores.push("La contraseña es obligatoria");
  if (errores.length) return apiResponse(400, false, errores.join(". "));

  const [usuario] = await db.select().from(usuarios).where(eq(usuarios.email, email));
  // Mismo mensaje genérico si el correo no existe o la contraseña no coincide
  if (!usuario || !(await bcrypt.compare(password, usuario.password)))
    return apiResponse(401, false, "Correo o contraseña incorrectos");

  const token = await crearSesion(usuario.id);
  return apiResponse(200, true, "Autenticación exitosa", { usuario: usuarioResponse(usuario), token });
};

export default async (req: Request, context: Context) => {
  const accion = context.params.accion;
  if (accion === "registro" && req.method === "POST") return registrar(req);
  if (accion === "login" && req.method === "POST") return iniciarSesion(req);
  if (accion === "logout" && req.method === "POST") {
    await cerrarSesion(req);
    return apiResponse(200, true, "Sesión cerrada");
  }
  if (accion === "yo" && req.method === "GET") {
    const usuario = await usuarioActual(req);
    if (!usuario) return noAutenticado();
    return apiResponse(200, true, "Sesión activa", { usuario: usuarioResponse(usuario) });
  }
  return apiResponse(404, false, "Recurso no encontrado");
};

export const config: Config = { path: "/api/auth/:accion" };
