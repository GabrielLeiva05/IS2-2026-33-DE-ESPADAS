package com.ejercicioIntegrador.dulcemayor.exceptions;

/**
 * Un servicio externo (geocodificación de Nominatim) no respondió. Se informa como 503 para que la app
 * le ofrezca al usuario la alternativa manual (marcar la ubicación en el mapa).
 */
public class ServicioExternoNoDisponibleException extends MiException {

    public ServicioExternoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
