package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.dto.CambioClaveFormularioDTO;
import com.dulcemayor.institucional.dto.LoginFormularioDTO;
import com.dulcemayor.institucional.dto.RegistroFormularioDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.seguridad.SesionCliente;
import com.dulcemayor.institucional.service.ServicioAcceso;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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
            SesionCliente sesion = servicioAcceso.ingresar(formulario);
            gestorSesion.iniciar(request, sesion);
            return sesion.isDebeCambiarClave() ? "redirect:/cambiar-clave" : "redirect:" + gestorSesion.tomarDestino(request);
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

    /**
     * Cambio obligatorio de la clave temporal "dulcemayor" (clientes dados de alta por el preventista).
     * Después vuelve a la página que el cliente quería abrir (por ejemplo, el enlace a su pedido).
     */
    @GetMapping("/cambiar-clave")
    public String cambiarClave(HttpServletRequest request, Model model) {
        Optional<SesionCliente> sesion = gestorSesion.actual(request);
        if (sesion.isEmpty()) {
            return "redirect:/ingresar";
        }
        if (!sesion.get().isDebeCambiarClave()) {
            return "redirect:/mi-cuenta";
        }
        model.addAttribute("formulario", new CambioClaveFormularioDTO());
        return "cambiar-clave";
    }

    @PostMapping("/cambiar-clave")
    public String cambiarClave(@ModelAttribute("formulario") CambioClaveFormularioDTO formulario,
                               HttpServletRequest request, Model model, RedirectAttributes redireccion) {
        Optional<SesionCliente> sesion = gestorSesion.actual(request);
        if (sesion.isEmpty()) {
            return "redirect:/ingresar";
        }
        try {
            gestorSesion.actualizar(request, servicioAcceso.cambiarClave(sesion.get(), formulario));
            redireccion.addFlashAttribute("exito", "Listo, ya elegiste tu contraseña.");
            return "redirect:" + gestorSesion.tomarDestino(request);
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
        return "redirect:/?salida";
    }
}
