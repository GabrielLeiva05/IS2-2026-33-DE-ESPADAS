package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Se ejecuta en TODAS las paginas (menos recursos estaticos). En cada peticion vuelve a leer al usuario de la
 * base de datos a partir del id guardado en la sesion:
 *  - Si el usuario fue eliminado (por un admin o por si mismo), la sesion se invalida en el acto: ya no puede seguir navegando.
 *  - Si le cambiaron el rol, el cambio rige de inmediato (el rol NUNCA se toma de la sesion, siempre de la base).
 * El usuario actualizado queda disponible como atributo de la peticion (SesionUsuario.ACTUAL).
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    private final UsuarioService usuarioService;

    public SesionInterceptor(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return true;
        }
        Object id = sesion.getAttribute(SesionUsuario.ID);
        if (!(id instanceof String)) {
            return true;
        }
        Optional<Usuario> usuario = Optional.empty();
        try {
            usuario = usuarioService.obtener((String) id);
        } catch (ErrorServiceException e) {
            // ante un error de base de datos se trata como "sin sesion" (modo seguro)
        }
        if (usuario.isPresent()) {
            request.setAttribute(SesionUsuario.ACTUAL, usuario.get());
        } else {
            sesion.invalidate();
        }
        return true;
    }
}
