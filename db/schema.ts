import { pgTable, serial, varchar, text, timestamp, integer } from "drizzle-orm/pg-core";

// Equivalente a la entidad JPA Usuario (tabla "usuarios")
export const usuarios = pgTable("usuarios", {
  id: serial().primaryKey(),
  nombre: varchar({ length: 100 }).notNull(),
  email: varchar({ length: 100 }).notNull().unique(),
  password: text().notNull(), // hash BCrypt, nunca texto plano
  telefono: varchar({ length: 20 }),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

// Equivalente a la entidad JPA Reporte (tabla "reportes")
export const reportes = pgTable("reportes", {
  id: serial().primaryKey(),
  titulo: varchar({ length: 150 }).notNull(),
  descripcion: varchar({ length: 1000 }).notNull(),
  categoria: varchar({ length: 50 }).notNull(), // robo, vandalismo, accidente, sospechoso
  ubicacion: varchar({ length: 200 }).notNull(),
  fechaReporte: timestamp("fecha_reporte").defaultNow().notNull(),
  estado: varchar({ length: 20 }).notNull().default("pendiente"), // pendiente, en_revision, resuelto
  usuarioId: integer("usuario_id")
    .notNull()
    .references(() => usuarios.id, { onDelete: "cascade" }),
});

// Sesiones de inicio de sesión (token opaco guardado como hash SHA-256)
export const sesiones = pgTable("sesiones", {
  tokenHash: varchar("token_hash", { length: 64 }).primaryKey(),
  usuarioId: integer("usuario_id")
    .notNull()
    .references(() => usuarios.id, { onDelete: "cascade" }),
  expiraEn: timestamp("expira_en").notNull(),
});
