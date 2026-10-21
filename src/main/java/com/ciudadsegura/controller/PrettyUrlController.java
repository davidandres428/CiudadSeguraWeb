package com.ciudadsegura.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrettyUrlController {

    @GetMapping("/")
    public ResponseEntity<Resource> inicio() {
        return servir("index.html");
    }

    @GetMapping("/index.html")
    public ResponseEntity<Void> redirigirIndex(HttpServletRequest request) {
        return redirigir("/", request);
    }

    @GetMapping("/reportes")
    public ResponseEntity<Resource> reportes() {
        return servir("reportes.html");
    }

    @GetMapping("/reportes.html")
    public ResponseEntity<Void> redirigirReportes(HttpServletRequest request) {
        return redirigir("/reportes", request);
    }

    @GetMapping("/zonas")
    public ResponseEntity<Resource> zonas() {
        return servir("zonas.html");
    }

    @GetMapping("/zonas.html")
    public ResponseEntity<Void> redirigirZonas(HttpServletRequest request) {
        return redirigir("/zonas", request);
    }

    @GetMapping("/alertas")
    public ResponseEntity<Resource> alertas() {
        return servir("alertas.html");
    }

    @GetMapping("/alertas.html")
    public ResponseEntity<Void> redirigirAlertas(HttpServletRequest request) {
        return redirigir("/alertas", request);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<Resource> usuarios() {
        return servir("usuarios.html");
    }

    @GetMapping("/usuarios.html")
    public ResponseEntity<Void> redirigirUsuarios(HttpServletRequest request) {
        return redirigir("/usuarios", request);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Resource> dashboard() {
        return servir("dashboard.html");
    }

    @GetMapping("/dashboard.html")
    public ResponseEntity<Void> redirigirDashboard(HttpServletRequest request) {
        return redirigir("/dashboard", request);
    }

    @GetMapping("/login")
    public ResponseEntity<Resource> login() {
        return servir("login.html");
    }

    @GetMapping("/login.html")
    public ResponseEntity<Void> redirigirLogin(HttpServletRequest request) {
        return redirigir("/login", request);
    }

    @GetMapping({"/reportes/", "/zonas/", "/alertas/", "/usuarios/", "/dashboard/", "/login/"})
    public ResponseEntity<Void> redirigirBarraFinal(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        return redirigir(ruta.substring(0, ruta.length() - 1), request);
    }

    private ResponseEntity<Void> redirigir(String ruta, HttpServletRequest request) {
        String query = request.getQueryString();
        String destino = query == null ? ruta : ruta + "?" + query;
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .header(HttpHeaders.LOCATION, destino)
                .build();
    }

    private ResponseEntity<Resource> servir(String archivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/" + archivo));
    }
}
