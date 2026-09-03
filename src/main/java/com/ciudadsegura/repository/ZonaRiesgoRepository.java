package com.ciudadsegura.repository;

import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.NivelRiesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaRiesgoRepository extends JpaRepository<ZonaRiesgo, Long> {

    List<ZonaRiesgo> findByNivelRiesgo(NivelRiesgo nivelRiesgo);
}
