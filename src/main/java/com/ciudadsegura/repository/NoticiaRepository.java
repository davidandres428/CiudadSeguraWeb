package com.ciudadsegura.repository;

import com.ciudadsegura.model.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    List<Noticia> findAllByOrderByFechaPublicacionDesc();
}
