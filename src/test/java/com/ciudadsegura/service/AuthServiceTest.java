package com.ciudadsegura.service;

import com.ciudadsegura.dto.LoginRequest;
import com.ciudadsegura.dto.RegistroRequest;
import com.ciudadsegura.exception.CredencialesInvalidasException;
import com.ciudadsegura.exception.EmailDuplicadoException;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import com.ciudadsegura.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrar_conEmailNuevo_debeCifrarLaContrasenaYGuardar() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Carlos Ruiz");
        request.setEmail("carlos@example.com");
        request.setPassword("secreto123");

        when(usuarioRepository.existsByEmail("carlos@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = authService.registrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        assertNotEquals("secreto123", captor.getValue().getPassword(), "La contraseña debe quedar cifrada, no en texto plano");
        assertEquals("carlos@example.com", resultado.getEmail());
        assertEquals(RolUsuario.CIUDADANO, resultado.getRol());
    }

    @Test
    void registrar_conRolAdministrador_debeMantenerRolCiudadano() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Administradora");
        request.setEmail("admin@example.com");
        request.setPassword("secreto123");
        request.setRol(RolUsuario.ADMINISTRADOR);

        when(usuarioRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = authService.registrar(request);

        assertEquals(RolUsuario.CIUDADANO, resultado.getRol());
    }

    @Test
    void registrar_conEmailDuplicado_debeLanzarExcepcion() {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("existente@example.com");
        request.setNombre("Test");
        request.setPassword("123456");

        when(usuarioRepository.existsByEmail("existente@example.com")).thenReturn(true);

        assertThrows(EmailDuplicadoException.class, () -> authService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_noDebePermitirApropiarseDelCorreoDelAdministradorPrincipal() {
        RegistroRequest request = new RegistroRequest();
        request.setEmail("DAVID.ANDRESGALVIS@GMAIL.COM");
        request.setNombre("Suplantador");
        request.setPassword("secreto123");

        assertThrows(EmailDuplicadoException.class, () -> authService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_conCredencialesCorrectas_debeRetornarUsuario() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuario = new Usuario("Laura", "laura@example.com", encoder.encode("miClave123"));

        LoginRequest request = new LoginRequest();
        request.setEmail("laura@example.com");
        request.setPassword("miClave123");

        when(usuarioRepository.findByEmail("laura@example.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = authService.login(request);

        assertEquals("laura@example.com", resultado.getEmail());
    }

    @Test
    void login_conContrasenaIncorrecta_debeLanzarExcepcion() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuario = new Usuario("Laura", "laura@example.com", encoder.encode("miClave123"));

        LoginRequest request = new LoginRequest();
        request.setEmail("laura@example.com");
        request.setPassword("claveIncorrecta");

        when(usuarioRepository.findByEmail("laura@example.com")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    void login_conEmailInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("noexiste@example.com");
        request.setPassword("cualquiera");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    void login_delAdministradorPrincipal_debeAsignarRolAdministrador() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Usuario usuario = new Usuario(
                "David", AuthService.ADMINISTRADOR_PRINCIPAL, encoder.encode("miClave123"));
        LoginRequest request = new LoginRequest();
        request.setEmail(AuthService.ADMINISTRADOR_PRINCIPAL);
        request.setPassword("miClave123");
        when(usuarioRepository.findByEmail(AuthService.ADMINISTRADOR_PRINCIPAL))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = authService.login(request);

        assertEquals(RolUsuario.ADMINISTRADOR, resultado.getRol());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void loginSocial_deAdministradorPrincipalExigeCorreoVerificado() {
        assertThrows(CredencialesInvalidasException.class, () -> authService.loginSocial(
                AuthService.ADMINISTRADOR_PRINCIPAL, "David", false));
        verify(usuarioRepository, never()).save(any());
    }
}
