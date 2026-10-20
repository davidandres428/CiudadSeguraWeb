package com.ciudadsegura.security;

import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.model.enums.RolUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "0123456789-0123456789-0123456789-ab";

    private Usuario admin() {
        Usuario u = new Usuario("Ana", "ana@example.com", "hash");
        u.setRol(RolUsuario.ADMINISTRADOR);
        ReflectionTestUtils.setField(u, "id", 7L);
        return u;
    }

    @Test
    void generar_y_validar_devuelveClaims() {
        JwtService jwt = new JwtService(SECRET, 1);
        var claims = jwt.validar(jwt.generar(admin()));
        assertTrue(claims.isPresent());
        assertEquals("7", claims.get().getSubject());
        assertEquals("ADMINISTRADOR", claims.get().get("rol", String.class));
    }

    @Test
    void validar_conTokenManipuladoOFirmaDistinta_devuelveVacio() {
        JwtService jwt = new JwtService(SECRET, 1);
        String token = jwt.generar(admin());
        assertTrue(jwt.validar(token + "x").isEmpty());
        assertTrue(new JwtService("otra-clave-otra-clave-otra-clave-123", 1).validar(token).isEmpty());
        assertTrue(jwt.validar("basura").isEmpty());
    }

    @Test
    void constructor_conSecretCorto_falla() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 1));
    }
}
