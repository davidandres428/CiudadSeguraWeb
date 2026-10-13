import type { Config, Context } from "@netlify/functions";
import { and, desc, eq, type SQL } from "drizzle-orm";
import { db } from "../../db/index.js";
import { reportes, usuarios } from "../../db/schema.js";
import { apiResponse, leerJson, noAutenticado, texto, usuarioActual } from "../../lib/http.js";

const CATEGORIAS = ["robo", "vandalismo", "accidente", "sospechoso", "otro"];
const ESTADOS = ["pendiente", "en_revision", "resuelto"];

const columnas = {
  id: reportes.id,
  titulo: reportes.titulo,
  descripcion: reportes.descripcion,
  categoria: reportes.categoria,
  ubicacion: reportes.ubicacion,
  fechaReporte: reportes.fechaReporte,
  estado: reportes.estado,
  usuarioId: reportes.usuarioId,
  autor: usuarios.nombre,
};

const consulta = () => db.select(columnas).from(reportes).innerJoin(usuarios, eq(reportes.usuarioId, usuarios.id));

const validar = (body: Record<string, unknown>) => {
  const datos = {
    titulo: texto(body.titulo),
    descripcion: texto(body.descripcion),
    categoria: texto(body.categoria).toLowerCase(),
    ubicacion: texto(body.ubicacion),
  };
  const errores: string[] = [];
  if (!datos.titulo) errores.push("El título es obligatorio");
  else if (datos.titulo.length > 150) errores.push("El título no puede superar 150 caracteres");
  if (!datos.descripcion) errores.push("La descripción es obligatoria");
  else if (datos.descripcion.length > 1000) errores.push("La descripción no puede superar 1000 caracteres");
  if (!CATEGORIAS.includes(datos.categoria)) errores.push("La categoría no es válida");
  if (!datos.ubicacion) errores.push("La ubicación es obligatoria");
  else if (datos.ubicacion.length > 200) errores.push("La ubicación no puede superar 200 caracteres");
  return { datos, errores };
};

export default async (req: Request, context: Context) => {
  const id = context.params.id ? Number(context.params.id) : null;
  if (context.params.id && !Number.isInteger(id)) return apiResponse(404, false, "Reporte no encontrado");

  // GET /api/reportes (filtros opcionales ?estado= y ?categoria=)
  if (req.method === "GET" && id === null) {
    const url = new URL(req.url);
    const filtros: SQL[] = [];
    const estado = url.searchParams.get("estado");
    const categoria = url.searchParams.get("categoria");
    if (estado) filtros.push(eq(reportes.estado, estado));
    if (categoria) filtros.push(eq(reportes.categoria, categoria));
    const lista = await consulta().where(and(...filtros)).orderBy(desc(reportes.fechaReporte));
    return Response.json(lista);
  }

  // GET /api/reportes/:id
  if (req.method === "GET") {
    const [reporte] = await consulta().where(eq(reportes.id, id!));
    return reporte ? Response.json(reporte) : apiResponse(404, false, "Reporte no encontrado");
  }

  // Crear, editar y eliminar requieren sesión
  const usuario = await usuarioActual(req);
  if (!usuario) return noAutenticado();

  // POST /api/reportes
  if (req.method === "POST" && id === null) {
    const { datos, errores } = validar(await leerJson(req));
    if (errores.length) return apiResponse(400, false, errores.join(". "));
    const [creado] = await db.insert(reportes).values({ ...datos, usuarioId: usuario.id }).returning();
    return Response.json({ ...creado, autor: usuario.nombre }, { status: 201 });
  }

  if (id === null) return apiResponse(405, false, "Método no permitido");

  const [existente] = await db.select().from(reportes).where(eq(reportes.id, id));
  if (!existente) return apiResponse(404, false, "Reporte no encontrado");
  if (existente.usuarioId !== usuario.id)
    return apiResponse(403, false, "Solo el autor puede modificar este reporte");

  // PUT /api/reportes/:id
  if (req.method === "PUT") {
    const body = await leerJson(req);
    const { datos, errores } = validar(body);
    const estado = texto(body.estado) || existente.estado;
    if (!ESTADOS.includes(estado)) errores.push("El estado no es válido");
    if (errores.length) return apiResponse(400, false, errores.join(". "));
    const [actualizado] = await db
      .update(reportes)
      .set({ ...datos, estado })
      .where(eq(reportes.id, id))
      .returning();
    return Response.json({ ...actualizado, autor: usuario.nombre });
  }

  // DELETE /api/reportes/:id
  if (req.method === "DELETE") {
    await db.delete(reportes).where(eq(reportes.id, id));
    return new Response(null, { status: 204 });
  }

  return apiResponse(405, false, "Método no permitido");
};

export const config: Config = { path: ["/api/reportes", "/api/reportes/:id"] };
