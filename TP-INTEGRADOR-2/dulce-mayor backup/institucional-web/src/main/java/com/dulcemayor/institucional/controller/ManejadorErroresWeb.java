package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.seguridad.GestorSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** 401 de la API: sesión vencida, se vuelve a ingresar. El resto de los errores se muestra con su mensaje. */
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
            return "redirect:/ingresar?expirada";
        }
        response.setStatus(error.getEstado());
        model.addAttribute("status", error.getEstado());
        model.addAttribute("mensaje", error.getMessage());
        return "error";
    }
}
