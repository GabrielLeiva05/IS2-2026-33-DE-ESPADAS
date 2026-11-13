package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.dto.CambioClaveFormularioDTO;
import com.dulcemayor.admin.dto.LoginFormularioDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import com.dulcemayor.admin.service.ServicioAcceso;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ingreso, cambio de contraseña y salida de empleados. Sin botones de redes sociales (solo clientes). */
@Controller
public class ControladorAcceso {

    private final ServicioAcceso servicioAcceso;
    private final GestorSesion gestorSesion;

    public ControladorAcceso(ServicioAcceso servicioAcceso, GestorSesion gestorSesion) {
        this.servicioAcceso = servicioAcceso;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (gestorSesion.actual(request).isPresent()) {
            return "redirect:/";
        }
        model.addAttribute("formulario", new LoginFormularioDTO());
        return "login";
    }

    @PostMapping("/login")
    public String ingresar(@ModelAttribute("formulario") LoginFormularioDTO formulario, HttpServletRequest request,
                           Model model) {
        try {
            SesionUsuario sesion = servicioAcceso.ingresar(formulario);
            gestorSesion.iniciar(request, sesion);
            return sesion.isDebeCambiarClave() ? "redirect:/cambiar-clave" : "redirect:/";
        } catch (ApiException e) {
            formulario.setClave(null);
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/cambiar-clave")
    public String cambiarClave(Model model) {
        model.addAttribute("formulario", new CambioClaveFormularioDTO());
        return "cambiar-clave";
    }

    @PostMapping("/cambiar-clave")
    public String guardarClave(@ModelAttribute("formulario") CambioClaveFormularioDTO formulario,
                               HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        SesionUsuario actual = gestorSesion.actual(request).orElseThrow();
        try {
            gestorSesion.actualizar(request, servicioAcceso.cambiarClave(actual, formulario));
            redireccion.addFlashAttribute("exito", "Tu contraseña se actualizó correctamente.");
            return "redirect:/";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            model.addAttribute("formulario", new CambioClaveFormularioDTO());
            model.addAttribute("error", e.getMessage());
            return "cambiar-clave";
        }
    }

    @PostMapping("/salir")
    public String salir(HttpServletRequest request) {
        gestorSesion.cerrar(request);
        return "redirect:/login?salida";
    }
}
