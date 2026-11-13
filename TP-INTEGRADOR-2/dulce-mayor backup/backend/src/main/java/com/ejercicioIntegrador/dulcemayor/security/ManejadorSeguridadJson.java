package com.ejercicioIntegrador.dulcemayor.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Respuestas JSON (mismo formato que ErrorDTO) para los errores que corta Spring Security antes de llegar
 * al controlador. Adaptado de JsonSecurityErrorHandler de Blockazo.
 *  - 401: sin token, token inválido o vencido.
 *  - 403: sin el rol necesario. Si el motivo es el cambio de clave pendiente, lo indica en el mensaje
 *         y en el encabezado X-Accion-Requerida: CAMBIAR_CLAVE.
 */
@Component
public class ManejadorSeguridadJson implements AuthenticationEntryPoint, AccessDeniedHandler {

    public static final String ENCABEZADO_ACCION = "X-Accion-Requerida";
    public static final String ACCION_CAMBIAR_CLAVE = "CAMBIAR_CLAVE";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Debe iniciar sesión o su sesión expiró");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        if (tieneCambioDeClavePendiente(SecurityContextHolder.getContext().getAuthentication())) {
            response.setHeader(ENCABEZADO_ACCION, ACCION_CAMBIAR_CLAVE);
            escribir(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                    "Debe cambiar su contraseña temporal antes de continuar");
            return;
        }
        escribir(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "No tiene permisos para realizar esta operación");
    }

    static boolean tieneCambioDeClavePendiente(Authentication autenticacion) {
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(a -> AutoridadesSeguridad.CAMBIO_CLAVE_PENDIENTE.equals(a.getAuthority()));
    }

    private static void escribir(HttpServletResponse response, int estado, String error, String mensaje)
            throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"estado\":" + estado
                + ",\"error\":\"" + error + "\""
                + ",\"mensaje\":\"" + mensaje + "\""
                + ",\"fecha\":\"" + LocalDateTime.now() + "\"}");
    }
}
