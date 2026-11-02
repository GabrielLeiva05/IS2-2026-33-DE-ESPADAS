package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.seguridad.GestorSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Errores de la API que no maneja cada pantalla:
 *  - 401 (token vencido o inválido): se cierra la sesión y se vuelve al login;
 *  - 403 con X-Accion-Requerida = CAMBIAR_CLAVE: se lleva al cambio de contraseña;
 *  - el resto se muestra en la página de error con el mensaje de la API.
 */
@ControllerAdvice
public class ManejadorErroresWeb {

    private final GestorSesion gestorSesion;

    public ManejadorErroresWeb(GestorSesion gestorSesion) {
        this.gestorSesion = gestorSesion;
    }

    @ExceptionHandler(ApiException.class)
    public String errorDeApi(ApiException error, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (error.esNoAutenticado()) {
            gestorSesion.cerrar(request);
            return "redirect:/login?expirada";
        }
        if (error.requiereCambioDeClave()) {
            return "redirect:/cambiar-clave";
        }
        response.setStatus(error.getEstado());
        model.addAttribute("status", error.getEstado());
        model.addAttribute("mensaje", error.getMessage());
        return "error";
    }
}
