package com.competencia.blockazo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/** Si el login con Google falla (cancelado, error del proveedor, etc.) vuelve al frontend con ?error=... */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String redirectFrontend;

    public OAuth2LoginFailureHandler(@Value("${app.frontend.oauth2-redirect}") String redirectFrontend) {
        this.redirectFrontend = redirectFrontend;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.sendRedirect(UriComponentsBuilder.fromUriString(redirectFrontend)
                .queryParam("error", "No se pudo iniciar sesion con el proveedor externo")
                .build().encode().toUriString());
    }
}
