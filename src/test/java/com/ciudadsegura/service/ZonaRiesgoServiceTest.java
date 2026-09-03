package com.ciudadsegura.service;

import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.ZonaRiesgo;
import com.ciudadsegura.model.enums.NivelRiesgo;
import com.ciudadsegura.repository.ZonaRiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaRiesgoServiceTest {

    @Mock
    private ZonaRiesgoRepository zonaRiesgoRepository;

    @InjectMocks
    private ZonaRiesgoService zonaRiesgoService;

    private ZonaRiesgo zona;

    @BeforeEach
    void setUp() {
        zona = new ZonaRiesgo();
        zona.setId(1L);
        zona.setNombre("Centro histórico");
        zona.setNivelRiesgo(NivelRiesgo.ALTO);
        zona.setLatitud(4.598);
        zona.setLongitud(-74.076);
    }

    @Test
    void listarPorNivel_debeFiltrarCorrectamente() {
        when(zonaRiesgoRepository.findByNivelRiesgo(NivelRiesgo.ALTO)).thenReturn(List.of(zona));

        List<ZonaRiesgo> resultado = zonaRiesgoService.listarPorNivel(NivelRiesgo.ALTO);

        assertEquals(1, resultado.size());
        assertEquals(NivelRiesgo.ALTO, resultado.get(0).getNivelRiesgo());
    }

    @Test
    void crear_debeGuardarLaZona() {
        when(zonaRiesgoRepository.save(any(ZonaRiesgo.class))).thenReturn(zona);

        ZonaRiesgo resultado = zonaRiesgoService.crear(zona);

        assertEquals("Centro histórico", resultado.getNombre());
        verify(zonaRiesgoRepository, times(1)).save(zona);
    }

    @Test
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        when(zonaRiesgoRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> zonaRiesgoService.buscarPorId(50L));
    }

    @Test
    void actualizar_debeSobrescribirLosDatos() {
        ZonaRiesgo datosNuevos = new ZonaRiesgo();
        datosNuevos.setNombre("Zona industrial");
        datosNuevos.setDescripcion("Poca iluminación en la noche");
        datosNuevos.setNivelRiesgo(NivelRiesgo.MEDIO);
        datosNuevos.setLatitud(4.6);
        datosNuevos.setLongitud(-74.08);

        when(zonaRiesgoRepository.findById(1L)).thenReturn(Optional.of(zona));
        when(zonaRiesgoRepository.save(any(ZonaRiesgo.class))).thenAnswer(inv -> inv.getArgument(0));

        ZonaRiesgo actualizada = zonaRiesgoService.actualizar(1L, datosNuevos);

        assertEquals("Zona industrial", actualizada.getNombre());
        assertEquals(NivelRiesgo.MEDIO, actualizada.getNivelRiesgo());
    }
}
