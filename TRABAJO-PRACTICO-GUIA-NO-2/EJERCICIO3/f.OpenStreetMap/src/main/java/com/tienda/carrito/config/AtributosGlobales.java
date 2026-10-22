package com.tienda.carrito.config;

import com.tienda.carrito.business.domain.entity.Rol;
import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.service.CarritoService;
import com.tienda.carrito.controller.view.AuthController;
import com.tienda.carrito.controller.view.BaseController;
import com.tienda.carrito.controller.view.CarritoController;
import com.tienda.carrito.controller.view.FalloController;
import com.tienda.carrito.controller.view.InicioController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Agrega a las vistas HTML los datos del menu: usuario logueado, si es administrador (para mostrar u ocultar
 * "Administracion") y cantidad de articulos del carrito. Se basa en el usuario ACTUAL leido de la base,
 * no en lo que haya quedado en la sesion.
 */
@ControllerAdvice(assignableTypes = { BaseController.class, InicioController.class, CarritoController.class,
        AuthController.class, FalloController.class })
public class AtributosGlobales {

    private final CarritoService carritoService;

    public AtributosGlobales(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @ModelAttribute
    public void cargarDatosDeSesion(HttpServletRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute(SesionUsuario.ACTUAL);
        model.addAttribute("usuarioLogueado", usuario != null);
        model.addAttribute("usuarioNombre", usuario != null ? usuario.getNombre() : null);
        model.addAttribute("esAdmin", usuario != null && usuario.getRol() == Rol.ADMIN);
        model.addAttribute("cantidadCarrito", usuario == null ? 0 : carritoService.contarArticulos(usuario.getId()));
    }
}
