package com.dulcemayor.admin.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Control de navegación de admin-web, antes de llegar a cualquier controlador:
 *  1) sin sesión -> al login;
 *  2) con la clave temporal pendiente -> solo /cambiar-clave y /salir (HU-06);
 *  3) cada sección exige el rol que corresponde.
 * Es una defensa de la interfaz: la autorización definitiva la vuelve a aplicar la API en cada pedido.
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
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        boolean pedidoJson = ruta.startsWith("/ubicacion/") || ruta.startsWith("/estadisticas/")
                || ruta.equals("/productos/subcategorias");
        Optional<SesionUsuario> sesion = gestorSesion.actual(request);

        if (sesion.isEmpty()) {
            if (pedidoJson) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }
        SesionUsuario usuario = sesion.get();
        if (usuario.isDebeCambiarClave() && !ruta.equals("/cambiar-clave") && !ruta.equals("/salir")) {
            if (pedidoJson) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.sendRedirect(request.getContextPath() + "/cambiar-clave");
            }
            return false;
        }
        if (!rolHabilitado(ruta).test(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /** Matriz de navegación por sección (coherente con la matriz de permisos de la API). */
    private static Predicate<SesionUsuario> rolHabilitado(String ruta) {
        if (ruta.startsWith("/empleados") || ruta.startsWith("/ubicacion/")) {
            return SesionUsuario::isJefe;
        }
        if (ruta.startsWith("/clientes") || ruta.startsWith("/rutas") || ruta.startsWith("/entregas")) {
            return SesionUsuario::isAdministrativo;
        }
        if (ruta.startsWith("/catalogo")) {
            return SesionUsuario::isPuedeVerCatalogo;
        }
        return usuario -> true;
    }
}
