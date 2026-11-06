package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.dto.LoginFormularioDTO;
import com.dulcemayor.institucional.dto.RegistroFormularioDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.service.ServicioAcceso;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ingreso y registro de clientes (HU-09), con correo y contraseña o con Google/Facebook. */
@Controller
public class ControladorAcceso {

    private final ServicioAcceso servicioAcceso;
    private final GestorSesion gestorSesion;

    public ControladorAcceso(ServicioAcceso servicioAcceso, GestorSesion gestorSesion) {
        this.servicioAcceso = servicioAcceso;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping("/ingresar")
    public String ingresar(HttpServletRequest request, Model model) {
        if (gestorSesion.actual(request).isPresent()) {
            return "redirect:/mi-cuenta";
        }
        model.addAttribute("formulario", new LoginFormularioDTO());
        return "ingresar";
    }

    @PostMapping("/ingresar")
    public String ingresar(@ModelAttribute("formulario") LoginFormularioDTO formulario, HttpServletRequest request,
                           Model model) {
        try {
            gestorSesion.iniciar(request, servicioAcceso.ingresar(formulario));
            return "redirect:" + gestorSesion.tomarDestino(request);
        } catch (ApiException e) {
            formulario.setClave(null);
            model.addAttribute("error", e.getMessage());
            return "ingresar";
        }
    }

    /**
     * Destino de la redirección de la API después de Google/Facebook: trae un código de un solo uso
     * (nunca el token) o un mensaje de error.
     */
    @GetMapping("/ingresar/oauth2")
    public String volverDeProveedor(@RequestParam(required = false) String codigo,
                                    @RequestParam(required = false) String error,
                                    HttpServletRequest request, RedirectAttributes redireccion) {
        if (codigo == null || codigo.isBlank()) {
            redireccion.addFlashAttribute("error", error == null || error.isBlank()
                    ? "No se pudo completar el ingreso con el proveedor externo." : error);
            return "redirect:/ingresar";
        }
        try {
            gestorSesion.iniciar(request, servicioAcceso.canjearCodigo(codigo));
            return "redirect:" + gestorSesion.tomarDestino(request);
        } catch (ApiException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingresar";
        }
    }

    @GetMapping("/registro")
    public String registro(HttpServletRequest request, Model model) {
        if (gestorSesion.actual(request).isPresent()) {
            return "redirect:/mi-cuenta";
        }
        model.addAttribute("formulario", new RegistroFormularioDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute("formulario") RegistroFormularioDTO formulario, HttpServletRequest request,
                            Model model, RedirectAttributes redireccion) {
        try {
            gestorSesion.iniciar(request, servicioAcceso.registrar(formulario));
            redireccion.addFlashAttribute("exito", "¡Bienvenido a Dulce Mayor! Tu cuenta ya está activa.");
            return "redirect:/mi-cuenta";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            formulario.setClave(null);
            formulario.setConfirmacionClave(null);
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @PostMapping("/salir")
    public String salir(HttpServletRequest request) {
        gestorSesion.cerrar(request);
        return "redirect:/?salida";
    }
}
