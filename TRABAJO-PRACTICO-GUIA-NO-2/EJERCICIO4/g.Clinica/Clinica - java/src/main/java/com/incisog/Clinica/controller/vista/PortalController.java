package com.incisog.Clinica.controller.vista;

import com.incisog.Clinica.logic.dto.adapter.UsuarioAdapter;
import com.incisog.Clinica.logic.error.ErrorServiceException;
import com.incisog.Clinica.logic.service.UsuarioService;
import com.incisog.Clinica.models.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FRONTEND JAVA — inicio, login y logout.
 *
 * Diferencia clave con los @RestController:
 *   - @RestController devuelve DATOS (JSON) y el que arma la pantalla es React.
 *   - @Controller devuelve el NOMBRE DE UNA VISTA ("login" → templates/login.html);
 *     Thymeleaf la completa con los datos del ModelMap y el navegador recibe HTML ya armado.
 *
 * El "usuario logueado" vive en la SESIÓN HTTP (del lado del servidor), igual que en el
 * tinder ("usuariosession"). En React lo guardábamos en localStorage (del lado del navegador).
 */
@Controller
public class PortalController {

    /** Nombre del atributo de sesión: lo usan todos los controllers de vistas. */
    public static final String USUARIO_SESION = "usuariosession";

    private final UsuarioService usuarioService;
    private final UsuarioAdapter usuarioAdapter;

    public PortalController(UsuarioService usuarioService, UsuarioAdapter usuarioAdapter) {
        this.usuarioService = usuarioService;
        this.usuarioAdapter = usuarioAdapter;
    }

    @GetMapping({"/", "/inicio"})
    public String inicio(HttpSession session) {
        return session.getAttribute(USUARIO_SESION) == null ? "redirect:/login" : "redirect:/pacientes";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute(USUARIO_SESION) != null) return "redirect:/pacientes"; // ya logueado
        return "login";
    }

    /** Recibe el <form method="post"> de login.html. Cada @RequestParam es un "name" del formulario. */
    @PostMapping("/login")
    public String ingresar(HttpSession session, ModelMap model,
                           @RequestParam String nombreUsuario, @RequestParam String clave) {
        try {
            Usuario usuario = usuarioService.login(nombreUsuario, clave);
            // Se guarda el DTO (sin clave), no la entidad: la clave no tiene por qué andar en la sesión.
            session.setAttribute(USUARIO_SESION, usuarioAdapter.toDTO(usuario));
            return "redirect:/pacientes";
        } catch (ErrorServiceException exception) {
            model.put("error", exception.getMessage());   // "Usuario o clave incorrectos"
            model.put("nombreUsuario", nombreUsuario);    // para no tener que escribirlo de nuevo
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
