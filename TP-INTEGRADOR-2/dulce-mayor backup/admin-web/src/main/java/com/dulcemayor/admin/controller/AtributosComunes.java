package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos que usan todas las vistas (Thymeleaf 3.1 ya no expone #request ni #session):
 * la sesión del empleado, la ruta actual para resaltar el menú, la URL pública de la API (imágenes)
 * y la del sitio institucional.
 */
@ControllerAdvice
public class AtributosComunes {

    private final GestorSesion gestorSesion;
    private final String apiPublica;
    private final String sitioUrl;

    public AtributosComunes(GestorSesion gestorSesion, @Value("${api.public-url}") String apiPublica,
                            @Value("${app.sitio-url}") String sitioUrl) {
        this.gestorSesion = gestorSesion;
        this.apiPublica = apiPublica;
        this.sitioUrl = sitioUrl;
    }

    @ModelAttribute("sesion")
    public SesionUsuario sesion(HttpServletRequest request) {
        return gestorSesion.actual(request).orElse(null);
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    @ModelAttribute("apiPublica")
    public String apiPublica() {
        return apiPublica;
    }

    /** Dirección del sitio institucional, para derivar a los clientes que intentan ingresar aquí. */
    @ModelAttribute("sitioUrl")
    public String sitioUrl() {
        return sitioUrl;
    }
}
