CREATE TABLE "reportes" (
	"id" serial PRIMARY KEY,
	"titulo" varchar(150) NOT NULL,
	"descripcion" varchar(1000) NOT NULL,
	"categoria" varchar(50) NOT NULL,
	"ubicacion" varchar(200) NOT NULL,
	"fecha_reporte" timestamp DEFAULT now() NOT NULL,
	"estado" varchar(20) DEFAULT 'pendiente' NOT NULL,
	"usuario_id" integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE "sesiones" (
	"token_hash" varchar(64) PRIMARY KEY,
	"usuario_id" integer NOT NULL,
	"expira_en" timestamp NOT NULL
);
--> statement-breakpoint
CREATE TABLE "usuarios" (
	"id" serial PRIMARY KEY,
	"nombre" varchar(100) NOT NULL,
	"email" varchar(100) NOT NULL UNIQUE,
	"password" text NOT NULL,
	"telefono" varchar(20),
	"created_at" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "reportes" ADD CONSTRAINT "reportes_usuario_id_usuarios_id_fkey" FOREIGN KEY ("usuario_id") REFERENCES "usuarios"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "sesiones" ADD CONSTRAINT "sesiones_usuario_id_usuarios_id_fkey" FOREIGN KEY ("usuario_id") REFERENCES "usuarios"("id") ON DELETE CASCADE;