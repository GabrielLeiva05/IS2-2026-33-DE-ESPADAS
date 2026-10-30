package com.competencia.blockazo.security;

import com.competencia.blockazo.error.ErrorServiceException;
import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Se ejecuta cuando Google (o Auth0) confirma la identidad del usuario.
 * 1) Busca el usuario por correo y, si no existe, lo da de alta (sin clave).
 * 2) Genera NUESTRO JWT.
 * 3) Redirige al frontend: {app.frontend.oauth2-redirect}?token=<jwt>
 * A partir de ahi el frontend usa el token igual que con el login comun.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final String redirectFrontend;

    public OAuth2LoginSuccessHandler(UsuarioService usuarioService,
                                     JwtService jwtService,
                                     @Value("${app.frontend.oauth2-redirect}") String redirectFrontend) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.redirectFrontend = redirectFrontend;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String correo = oauthUser.getAttribute("email");
        Object verificado = oauthUser.getAttribute("email_verified");

        // Solo se aceptan correos verificados por el proveedor
        if (correo == null || !Boolean.parseBoolean(String.valueOf(verificado))) {
            redirigirConError(request, response, "El proveedor no devolvio un correo verificado");
            return;
        }

        Proveedor proveedor = Proveedor.GOOGLE;
        if (authentication instanceof OAuth2AuthenticationToken token
                && "auth0".equalsIgnoreCase(token.getAuthorizedClientRegistrationId())) {
            proveedor = Proveedor.AUTH0;
        }

        try {
            Usuario usuario = usuarioService.registrarOIngresarOAuth2(
                    correo.trim().toLowerCase(),
                    oauthUser.getAttribute("given_name"),
                    oauthUser.getAttribute("family_name"),
                    proveedor);

            String jwt = jwtService.generarToken(usuario);
            limpiarSesion(request);

            String destino = UriComponentsBuilder.fromUriString(redirectFrontend)
                    .queryParam("token", jwt)
                    .build().encode().toUriString();
            response.sendRedirect(destino);

        } catch (ErrorServiceException e) {
            redirigirConError(request, response, e.getMessage());
        }
    }

    private void redirigirConError(HttpServletRequest request, HttpServletResponse response,
                                   String mensaje) throws IOException {
        limpiarSesion(request);
        response.sendRedirect(UriComponentsBuilder.fromUriString(redirectFrontend)
                .queryParam("error", mensaje)
                .build().encode().toUriString());
    }

    /** La API es stateless: la sesion solo se uso durante el "baile" OAuth2. */
    private void limpiarSesion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
