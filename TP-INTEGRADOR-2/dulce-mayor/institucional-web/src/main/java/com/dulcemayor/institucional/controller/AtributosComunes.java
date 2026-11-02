package com.dulcemayor.institucional.controller;

import com.dulcemayor.institucional.seguridad.GestorSesion;
import com.dulcemayor.institucional.seguridad.SesionCliente;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos comunes a todas las vistas: sesión del cliente, ruta actual, URL pública de la API
 * (imágenes y botones de Google/Facebook) y qué proveedores externos están habilitados.
 */
@ControllerAdvice
public class AtributosComunes {

    private final GestorSesion gestorSesion;
    private final String apiPublica;
    private final boolean googleHabilitado;
    private final boolean facebookHabilitado;

    public AtributosComunes(GestorSesion gestorSesion,
                            @Value("${api.public-url}") String apiPublica,
                            @Value("${app.oauth.google.habilitado:false}") boolean googleHabilitado,
                            @Value("${app.oauth.facebook.habilitado:false}") boolean facebookHabilitado) {
        this.gestorSesion = gestorSesion;
        this.apiPublica = apiPublica;
        this.googleHabilitado = googleHabilitado;
        this.facebookHabilitado = facebookHabilitado;
    }

    @ModelAttribute("sesion")
    public SesionCliente sesion(HttpServletRequest request) {
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

    /** Los botones solo se muestran si el proveedor está configurado en la API (ver CONFIGURACION-OAUTH.md). */
    @ModelAttribute("googleHabilitado")
    public boolean googleHabilitado() {
        return googleHabilitado;
    }

    @ModelAttribute("facebookHabilitado")
    public boolean facebookHabilitado() {
        return facebookHabilitado;
    }
}
