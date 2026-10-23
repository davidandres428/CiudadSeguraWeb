package com.ciudadsegura.controller;

import com.ciudadsegura.model.Noticia;
import com.ciudadsegura.repository.NoticiaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    private final NoticiaRepository noticiaRepository;

    public NoticiaController(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping
    public List<Noticia> listar() {
        return noticiaRepository.findAllByOrderByFechaPublicacionDesc();
    }
}
