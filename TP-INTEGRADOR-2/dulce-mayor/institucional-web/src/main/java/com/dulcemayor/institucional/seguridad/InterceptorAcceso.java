package com.dulcemayor.institucional.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * El sitio es público salvo "Mi cuenta". Sin sesión, se deriva al ingreso.
 * La API vuelve a exigir el rol CLIENTE en /api/clientes/yo.
 */
@Component
public class InterceptorAcceso implements HandlerInterceptor {

    private final GestorSesion gestorSesion;

    public InterceptorAcceso(GestorSesion gestorSesion) {
        this.gestorSesion = gestorSesion;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (gestorSesion.actual(request).isPresent()) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/ingresar");
        return false;
    }
}
