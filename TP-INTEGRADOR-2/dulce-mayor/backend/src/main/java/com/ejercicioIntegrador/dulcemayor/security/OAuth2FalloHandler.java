package com.ejercicioIntegrador.dulcemayor.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Si el ingreso con Google o Facebook falla (cancelado, credenciales mal configuradas, etc.) vuelve al sitio
 * institucional con un mensaje genérico. La causa real queda en el log del servidor (de Blockazo).
 */
public class OAuth2FalloHandler implements AuthenticationFailureHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2FalloHandler.class);

    private final String redireccionCliente;

    public OAuth2FalloHandler(String redireccionCliente) {
        this.redireccionCliente = redireccionCliente;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        LOG.warn("Ingreso OAuth2 fallido: {}", exception.getMessage());
        response.sendRedirect(UriComponentsBuilder.fromUriString(redireccionCliente)
                .queryParam("error", "No se pudo iniciar sesión con el proveedor externo")
                .build().encode().toUriString());
    }
}
