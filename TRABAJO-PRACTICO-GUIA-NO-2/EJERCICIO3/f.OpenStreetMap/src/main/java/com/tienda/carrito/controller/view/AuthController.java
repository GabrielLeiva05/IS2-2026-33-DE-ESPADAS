package com.tienda.carrito.controller.view;

import com.tienda.carrito.business.domain.entity.Usuario;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.logic.service.UsuarioService;
import com.tienda.carrito.config.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Login, logout y registro. El registro publico siempre crea usuarios CLIENTE (regla en UsuarioService). */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String formularioLogin() {
        return "view/login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String nombre,
                                @RequestParam String clave,
                                HttpServletRequest request,
                                Model model) {
        try {
            Usuario usuario = usuarioService.autenticar(nombre, clave);
            HttpSession sesion = request.getSession(true);
            request.changeSessionId(); // evita "session fixation"
            sesion.setAttribute(SesionUsuario.ID, usuario.getId());
            sesion.setAttribute(SesionUsuario.NOMBRE, usuario.getNombre());
            return "redirect:/";
        } catch (ErrorServiceException e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("nombre", nombre);
            return "view/login";
        }
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/";
    }

    @GetMapping("/registro")
    public String formularioRegistro() {
        return "view/registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                            @RequestParam String clave,
                            @RequestParam String confirmacion,
                            Model model,
                            RedirectAttributes attributes) {
        // Comparar las dos claves es validacion de formulario (no regla de dominio), por eso se hace aca.
        if (!clave.equals(confirmacion)) {
            model.addAttribute("msgError", "Las claves no coinciden.");
            model.addAttribute("nombre", nombre);
            return "view/registro";
        }
        try {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setClave(clave);
            usuarioService.registrarCliente(nuevo);
            attributes.addFlashAttribute("msgExito", "Cuenta creada. Ya podés iniciar sesión.");
            return "redirect:/login";
        } catch (ErrorServiceException e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("nombre", nombre);
            return "view/registro";
        }
    }
}
