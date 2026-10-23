package com.ciudadsegura.security;

import com.ciudadsegura.dto.AuthResponse;
import com.ciudadsegura.dto.UsuarioResponse;
import com.ciudadsegura.model.Usuario;
import com.ciudadsegura.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    public OAuthLoginSuccessHandler(
            AuthService authService,
            JwtTokenService jwtTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)
                || !(authentication.getPrincipal() instanceof OAuth2User principal)) {
            throw new ServletException("La autenticación social no tiene un usuario OAuth2 válido.");
        }

        String email = principal.getAttribute("email");
        String nombre = principal.getAttribute("name");
        boolean correoVerificado = principal instanceof OidcUser oidcUser
                && Boolean.TRUE.equals(oidcUser.getEmailVerified());

        Usuario usuario = authService.loginSocial(email, nombre, correoVerificado);
        AuthResponse authResponse = new AuthResponse(
                jwtTokenService.crearToken(usuario), new UsuarioResponse(usuario));
        UsuarioResponse usuarioResponse = authResponse.getUsuario();
        String token = URLEncoder.encode(authResponse.getAccessToken(), StandardCharsets.UTF_8);
        response.sendRedirect("/login#oauth?accessToken=" + token
                + "&id=" + usuarioResponse.getId()
                + "&nombre=" + codificar(usuarioResponse.getNombre())
                + "&email=" + codificar(usuarioResponse.getEmail())
                + "&rol=" + usuarioResponse.getRol().name()
                + "&fechaRegistro=" + codificar(usuarioResponse.getFechaRegistro().toString()));
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
