package com.ciudadsegura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Ciudad Segura.
 * Integra los módulos de Usuarios, Reportes de incidentes, Zonas de riesgo y Alertas
 * en una única aplicación Spring Boot (back-end REST + front-end estático + base de datos).
 */
@SpringBootApplication
public class CiudadSeguraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiudadSeguraApplication.class, args);
    }
}
