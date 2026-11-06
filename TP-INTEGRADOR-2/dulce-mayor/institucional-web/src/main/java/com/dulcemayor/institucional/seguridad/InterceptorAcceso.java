package com.dulcemayor.institucional.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * El sitio es público salvo "Mi cuenta" y "Mis pedidos". Sin sesión, se deriva al ingreso recordando la
 * página pedida (así el enlace a un pedido vuelve a ese pedido después de ingresar). La API vuelve a exigir
 * el rol CLIENTE y verifica que el pedido sea del cliente.
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
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String ruta = request.getRequestURI().substring(request.getContextPath().length());
            gestorSesion.recordarDestino(request, request.getQueryString() == null ? ruta
                    : ruta + "?" + request.getQueryString());
        }
        response.sendRedirect(request.getContextPath() + "/ingresar");
        return false;
    }
}
