package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Control de acceso por rol. Se registra (en WebConfig) sobre grupos de rutas:
 *   - rolRequerido = ADMIN  -> solo administradores (403 si es CLIENTE; al login si no inicio sesion).
 *   - rolRequerido = null   -> cualquier usuario logueado.
 * Funciona porque SesionInterceptor corre antes y deja el usuario ACTUAL (leido de la base) en la peticion.
 */
public class AutorizacionInterceptor implements HandlerInterceptor {

    private final Rol rolRequerido;

    public AutorizacionInterceptor(Rol rolRequerido) {
        this.rolRequerido = rolRequerido;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Usuario usuario = (Usuario) request.getAttribute(SesionUsuario.ACTUAL);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (rolRequerido != null && usuario.getRol() != rolRequerido) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
