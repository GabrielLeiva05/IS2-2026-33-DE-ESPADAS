package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.dto.CambioClaveFormularioDTO;
import com.dulcemayor.institucional.dto.ClienteDTO;
import com.dulcemayor.institucional.dto.DatosPersonalesFormularioDTO;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.seguridad.SesionCliente;
import com.dulcemayor.institucional.service.ServicioAcceso;
import com.dulcemayor.institucional.service.ServicioCuenta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** "Mi cuenta" del cliente: datos personales y cambio voluntario de contraseña. */
@Controller
@RequestMapping("/mi-cuenta")
public class ControladorCuenta {

    private final ServicioCuenta servicioCuenta;
    private final ServicioAcceso servicioAcceso;
    private final GestorSesion gestorSesion;

    public ControladorCuenta(ServicioCuenta servicioCuenta, ServicioAcceso servicioAcceso, GestorSesion gestorSesion) {
        this.servicioCuenta = servicioCuenta;
        this.servicioAcceso = servicioAcceso;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping
    public String miCuenta(HttpServletRequest request, Model model) {
        ClienteDTO cliente = servicioCuenta.misDatos(sesion(request).getToken());
        return vista(model, DatosPersonalesFormularioDTO.desde(cliente), new CambioClaveFormularioDTO());
    }

    @PostMapping("/datos")
    public String guardarDatos(@ModelAttribute("datos") DatosPersonalesFormularioDTO datos, HttpServletRequest request,
                               Model model, RedirectAttributes redireccion) {
        SesionCliente sesion = sesion(request);
        try {
            ClienteDTO actualizado = servicioCuenta.actualizarDatos(sesion.getToken(), datos);
            gestorSesion.actualizar(request, sesion.conNombre(actualizado.getNombre(), actualizado.getApellido()));
            redireccion.addFlashAttribute("exito", "Tus datos se guardaron correctamente.");
            return "redirect:/mi-cuenta";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            model.addAttribute("errorDatos", e.getMessage());
            return vista(model, datos, new CambioClaveFormularioDTO());
        }
    }

    @PostMapping("/clave")
    public String cambiarClave(@ModelAttribute("clave") CambioClaveFormularioDTO clave, HttpServletRequest request,
                               Model model, RedirectAttributes redireccion) {
        SesionCliente sesion = sesion(request);
        try {
            gestorSesion.actualizar(request, servicioAcceso.cambiarClave(sesion, clave));
            redireccion.addFlashAttribute("exito", "Tu contraseña se actualizó correctamente.");
            return "redirect:/mi-cuenta";
        } catch (ApiException e) {
            if (!e.esErrorDeDatos()) {
                throw e;
            }
            model.addAttribute("errorClave", e.getMessage());
            ClienteDTO cliente = servicioCuenta.misDatos(sesion.getToken());
            return vista(model, DatosPersonalesFormularioDTO.desde(cliente), new CambioClaveFormularioDTO());
        }
    }

    private String vista(Model model, DatosPersonalesFormularioDTO datos, CambioClaveFormularioDTO clave) {
        model.addAttribute("datos", datos);
        model.addAttribute("clave", clave);
        return "mi-cuenta";
    }

    private SesionCliente sesion(HttpServletRequest request) {
        return gestorSesion.actual(request).orElseThrow();
    }
}
