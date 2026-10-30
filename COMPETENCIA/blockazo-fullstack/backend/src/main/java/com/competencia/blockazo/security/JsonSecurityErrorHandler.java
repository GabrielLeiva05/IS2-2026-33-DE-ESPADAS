package com.competencia.blockazo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Respuestas JSON para los errores que corta Spring Security ANTES de llegar al controller:
 *  - 401: no hay token, el token es invalido o esta vencido.
 *  - 403: el usuario esta logueado pero no tiene el rol necesario (ej. ABM de usuarios sin ser ADMIN).
 * Mismo formato que GlobalExceptionHandler, asi el frontend siempre lee "mensaje".
 */
@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesion o su sesion expiro");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para realizar esta accion");
    }

    private void escribir(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":" + status
                + ",\"mensaje\":\"" + mensaje + "\""
                + ",\"fecha\":\"" + LocalDateTime.now() + "\"}");
    }
}
