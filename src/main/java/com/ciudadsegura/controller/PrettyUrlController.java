package com.ciudadsegura.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.DispatcherType;
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
    public String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/inicio")
    public String portada() {
        return "forward:/index.html";
    }

    @GetMapping("/index.html")
    public Object redirigirIndex(HttpServletRequest request) {
        return rutaHtml("index.html", "/inicio", request);
    }

    @GetMapping("/reportes")
    public String reportes() {
        return "forward:/reportes.html";
    }

    @GetMapping("/reportes.html")
    public Object redirigirReportes(HttpServletRequest request) {
        return rutaHtml("reportes.html", "/reportes", request);
    }

    @GetMapping("/zonas")
    public String zonas() {
        return "forward:/zonas.html";
    }

    @GetMapping("/zonas.html")
    public Object redirigirZonas(HttpServletRequest request) {
        return rutaHtml("zonas.html", "/zonas", request);
    }

    @GetMapping("/alertas")
    public String alertas() {
        return "forward:/alertas.html";
    }

    @GetMapping("/alertas.html")
    public Object redirigirAlertas(HttpServletRequest request) {
        return rutaHtml("alertas.html", "/alertas", request);
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "forward:/usuarios.html";
    }

    @GetMapping("/usuarios.html")
    public Object redirigirUsuarios(HttpServletRequest request) {
        return rutaHtml("usuarios.html", "/usuarios", request);
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/dashboard.html")
    public Object redirigirDashboard(HttpServletRequest request) {
        return rutaHtml("dashboard.html", "/dashboard", request);
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/login.html")
    public Object redirigirLogin(HttpServletRequest request) {
        return rutaHtml("login.html", "/login", request);
    }

    @GetMapping({"/inicio/", "/reportes/", "/zonas/", "/alertas/", "/usuarios/", "/dashboard/", "/login/"})
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

    private Object rutaHtml(String archivo, String rutaLimpia, HttpServletRequest request) {
        if (request.getDispatcherType() == DispatcherType.FORWARD) {
            return servir(archivo);
        }
        return redirigir(rutaLimpia, request);
    }

    private ResponseEntity<Resource> servir(String archivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/" + archivo));
    }
}
