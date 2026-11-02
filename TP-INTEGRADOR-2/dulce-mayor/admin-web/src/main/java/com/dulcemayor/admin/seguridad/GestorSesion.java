package com.dulcemayor.admin.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Guarda y recupera la sesión del empleado en la HttpSession. */
@Component
public class GestorSesion {

    static final String ATRIBUTO = "DM_SESION_EMPLEADO";

    public Optional<SesionUsuario> actual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return Optional.empty();
        }
        Object valor = sesion.getAttribute(ATRIBUTO);
        return valor instanceof SesionUsuario usuario ? Optional.of(usuario) : Optional.empty();
    }

    /** Inicia la sesión cambiando el identificador para evitar la fijación de sesión. */
    public void iniciar(HttpServletRequest request, SesionUsuario usuario) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute(ATRIBUTO, usuario);
    }

    /** Reemplaza los datos (por ejemplo, el token nuevo después de cambiar la clave). */
    public void actualizar(HttpServletRequest request, SesionUsuario usuario) {
        request.getSession(true).setAttribute(ATRIBUTO, usuario);
    }

    public void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
