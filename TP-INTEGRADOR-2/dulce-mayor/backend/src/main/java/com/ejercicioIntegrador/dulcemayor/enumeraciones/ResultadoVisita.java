package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/**
 * Resultado de cada parada de una ruta (agregado al diagrama en PuntoRuta, reemplaza "visitado").
 *  Rutas de VISITA:  CON_PEDIDO / SIN_PEDIDO.
 *  Rutas de ENTREGA: ENTREGADO / NO_ENTREGADO (intento fallido: el pedido sigue pendiente de entrega).
 */
public enum ResultadoVisita {
    PENDIENTE,
    CON_PEDIDO,
    SIN_PEDIDO,
    ENTREGADO,
    NO_ENTREGADO;

    public boolean esPendiente() {
        return this == PENDIENTE;
    }
}
