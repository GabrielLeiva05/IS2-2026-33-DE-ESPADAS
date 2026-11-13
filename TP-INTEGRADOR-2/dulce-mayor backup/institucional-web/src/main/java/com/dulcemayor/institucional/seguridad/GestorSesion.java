package com.dulcemayor.institucional.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Guarda y recupera la sesión del cliente en la HttpSession. */
@Component
public class GestorSesion {

    static final String ATRIBUTO = "DM_SESION_CLIENTE";
    static final String DESTINO = "DM_DESTINO";
    static final String DESTINO_POR_DEFECTO = "/mi-cuenta";

    public Optional<SesionCliente> actual(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return Optional.empty();
        }
        Object valor = sesion.getAttribute(ATRIBUTO);
        return valor instanceof SesionCliente usuario ? Optional.of(usuario) : Optional.empty();
    }

    /**
     * Inicia la sesión cambiando el identificador para evitar la fijación de sesión. Conserva la página
     * que el cliente quería abrir antes de ingresar (por ejemplo, el enlace a un pedido).
     */
    public void iniciar(HttpServletRequest request, SesionCliente usuario) {
        HttpSession anterior = request.getSession(false);
        Object destino = null;
        if (anterior != null) {
            destino = anterior.getAttribute(DESTINO);
            anterior.invalidate();
        }
        HttpSession nueva = request.getSession(true);
        nueva.setAttribute(ATRIBUTO, usuario);
        if (destino != null) {
            nueva.setAttribute(DESTINO, destino);
        }
    }

    /** Recuerda una página interna del sitio para volver a ella después del ingreso. */
    public void recordarDestino(HttpServletRequest request, String destino) {
        if (esDestinoInterno(destino)) {
            request.getSession(true).setAttribute(DESTINO, destino);
        }
    }

    /** Devuelve (y olvida) la página a la que hay que volver; si no hay ninguna, "Mi cuenta". */
    public String tomarDestino(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return DESTINO_POR_DEFECTO;
        }
        Object destino = sesion.getAttribute(DESTINO);
        sesion.removeAttribute(DESTINO);
        return destino instanceof String texto && esDestinoInterno(texto) ? texto : DESTINO_POR_DEFECTO;
    }

    /**
     * Solo rutas propias del sitio: empiezan con "/" y no pueden apuntar a otro dominio
     * ("//otro.com", "/\\otro.com", "https:") ni traer saltos de línea.
     */
    static boolean esDestinoInterno(String destino) {
        return destino != null && destino.length() <= 200 && destino.startsWith("/")
                && !destino.startsWith("//") && !destino.contains("\\") && !destino.contains(":")
                && !destino.contains("{") && !destino.contains("}")
                && destino.chars().noneMatch(c -> c < 0x20 || c == 0x7f);
    }

    /** Reemplaza los datos (por ejemplo, el token nuevo después de cambiar la clave). */
    public void actualizar(HttpServletRequest request, SesionCliente usuario) {
        request.getSession(true).setAttribute(ATRIBUTO, usuario);
    }

    public void cerrar(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
