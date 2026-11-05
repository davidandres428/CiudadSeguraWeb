package com.ciudadsegura.controller;

import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/{id}/rol")
    public String cambiarRol(@PathVariable Long id, @RequestParam RolUsuario rol) {
        usuarioService.cambiarRol(id, rol);
        return "redirect:/usuarios";
    }
}