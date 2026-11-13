package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

/**
 * Fuente externa de la cotización del dólar. Hoy la implementa DolarAPI (ProveedorCotizacionDolarApi); para
 * cambiar de fuente alcanza con otra implementación, sin tocar ServicioCotizacion ni el catálogo.
 */
public interface ProveedorCotizacion {

    /**
     * Consulta la cotización actual del tipo indicado.
     *
     * @throws IllegalStateException si la fuente no responde o devuelve datos inválidos
     */
    Cotizacion consultar(String tipo);
}
