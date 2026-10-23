package com.ciudadsegura.service;

import com.ciudadsegura.dto.UsuarioUpdateRequest;
import com.ciudadsegura.exception.RecursoNoEncontradoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ciudadsegura.model.enums.RolUsuario.ADMINISTRADOR;
import static com.ciudadsegura.model.enums.RolUsuario.CIUDADANO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ana Torres", "ana@example.com", "hashDeEjemplo");
        usuario.setId(1L);
    }

    @Test
    void listarTodos_debeRetornarTodosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Ana Torres", resultado.get(0).getNombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_conIdExistente_debeRetornarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertEquals("ana@example.com", resultado.getEmail());
    }

    @Test
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.buscarPorId(99L));
    }

    @Test
    void actualizar_conRolYPassword_debeCambiarRolYEncriptar() {
        UsuarioUpdateRequest datos = new UsuarioUpdateRequest();
        datos.setNombre(usuario.getNombre());
        datos.setEmail(usuario.getEmail());
        datos.setRol(com.ciudadsegura.model.enums.RolUsuario.ADMINISTRADOR);
        datos.setPassword("nueva123");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = usuarioService.actualizar(1L, datos);

        assertEquals(com.ciudadsegura.model.enums.RolUsuario.ADMINISTRADOR, actualizado.getRol());
        assertTrue(actualizado.getPassword().startsWith("$2"));
    }

    @Test
    void actualizar_conEmailDeOtroUsuario_debeLanzarExcepcion() {
        UsuarioUpdateRequest datos = new UsuarioUpdateRequest();
        datos.setNombre("Ana");
        datos.setEmail("otro@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("otro@example.com")).thenReturn(true);

        assertThrows(com.ciudadsegura.exception.EmailDuplicadoException.class,
                () -> usuarioService.actualizar(1L, datos));
    }

    @Test
    void actualizar_debeModificarNombreYEmail() {
        UsuarioUpdateRequest datos = new UsuarioUpdateRequest();
        datos.setNombre("Ana María Torres");
        datos.setEmail("ana.maria@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario actualizado = usuarioService.actualizar(1L, datos);

        assertEquals("Ana María Torres", actualizado.getNombre());
        assertEquals("ana.maria@example.com", actualizado.getEmail());
    }

    @Test
    void eliminar_conIdInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.eliminar(5L));
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    void eliminar_conIdExistente_debeEliminarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    void actualizar_noDebeQuitarElRolAlUltimoAdministrador() {
        usuario.setRol(ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRol(ADMINISTRADOR)).thenReturn(1L);
        UsuarioUpdateRequest datos = new UsuarioUpdateRequest();
        datos.setNombre(usuario.getNombre());
        datos.setEmail(usuario.getEmail());
        datos.setRol(CIUDADANO);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> usuarioService.actualizar(1L, datos));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizar_noDebeModificarLaCuentaPrincipal() {
        usuario.setEmail(AuthService.ADMINISTRADOR_PRINCIPAL);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        UsuarioUpdateRequest datos = new UsuarioUpdateRequest();
        datos.setNombre(usuario.getNombre());
        datos.setEmail(usuario.getEmail());
        datos.setRol(CIUDADANO);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> usuarioService.actualizar(1L, datos));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
