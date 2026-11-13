package com.dulcemayor.institucional.cliente;

/**
 * Error devuelto por la API de Dulce Mayor (o falta de conexión con ella), traducido a un mensaje legible.
 * accionRequerida trae el encabezado X-Accion-Requerida (por ejemplo CAMBIAR_CLAVE).
 */
public class ApiException extends RuntimeException {

    public static final String CAMBIAR_CLAVE = "CAMBIAR_CLAVE";

    private final int estado;
    private final String accionRequerida;

    public ApiException(int estado, String mensaje, String accionRequerida) {
        super(mensaje);
        this.estado = estado;
        this.accionRequerida = accionRequerida;
    }

    public int getEstado() {
        return estado;
    }

    public String getAccionRequerida() {
        return accionRequerida;
    }

    public boolean esNoAutenticado() {
        return estado == 401;
    }

    public boolean requiereCambioDeClave() {
        return estado == 403 && CAMBIAR_CLAVE.equals(accionRequerida);
    }

    public boolean esProhibido() {
        return estado == 403;
    }

    /** Errores de validación o de negocio que el usuario puede corregir en el formulario. */
    public boolean esErrorDeDatos() {
        return estado == 400 || estado == 404 || estado == 405 || estado == 409;
    }
}
