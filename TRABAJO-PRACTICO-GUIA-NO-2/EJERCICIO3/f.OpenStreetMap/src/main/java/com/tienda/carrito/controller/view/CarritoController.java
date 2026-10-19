package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Carrito;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.CarritoService;
import com.tienda.carrito.config.SesionUsuario;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Carrito del usuario logueado (ADMIN o CLIENTE; el interceptor ya garantiza que hay sesion valida).
 * Siempre se opera sobre el carrito del usuario de la SESION: el id nunca viene del formulario,
 * asi que nadie puede tocar el carrito de otra persona. No extiende BaseController: no es un CRUD generico.
 */
@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private static final Logger log = LoggerFactory.getLogger(CarritoController.class);

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public String ver(HttpSession sesion, Model model) {
        // Si el usuario todavia no tiene carrito, la vista muestra "carrito vacio"
        model.addAttribute("carrito", carritoService.buscarActivo(usuarioId(sesion)).orElse(null));
        return "view/carrito";
    }

    @PostMapping("/agregar/{articuloId}")
    public String agregar(@PathVariable String articuloId, HttpSession sesion, RedirectAttributes attributes) {
        try {
            carritoService.agregarArticulo(usuarioId(sesion), articuloId);
            attributes.addFlashAttribute("msgExito", "Artículo agregado al carrito.");
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/detalle/{detalleId}/quitar")
    public String quitar(@PathVariable String detalleId, HttpSession sesion, RedirectAttributes attributes) {
        try {
            carritoService.quitarDetalle(usuarioId(sesion), detalleId);
            attributes.addFlashAttribute("msgExito", "Artículo quitado del carrito.");
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession sesion, RedirectAttributes attributes) {
        try {
            carritoService.vaciar(usuarioId(sesion));
            attributes.addFlashAttribute("msgExito", "Carrito vaciado.");
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/confirmar")
    public String confirmar(HttpSession sesion, RedirectAttributes attributes) {
        try {
            Carrito cerrado = carritoService.confirmarCompra(usuarioId(sesion));
            NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
            attributes.addFlashAttribute("msgExito", "¡Compra confirmada! Total: " + moneda.format(cerrado.getTotal()));
            return "redirect:/";
        } catch (ErrorServiceException e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
            return "redirect:/carrito";
        }
    }

    private String usuarioId(HttpSession sesion) {
        return (String) sesion.getAttribute(SesionUsuario.ID);
    }
}
