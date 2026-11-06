package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;

/**
 * Capacidad adicional de los medios que reciben notificaciones asincrónicas del proveedor
 * (hoy solo Mercado Pago). Transferencia y efectivo no la implementan.
 */
public interface EstrategiaNotificable {

    MedioPago medio();

    /**
     * Valida la firma de la notificación y consulta al proveedor server-to-server.
     *
     * @return null si la notificación no corresponde a un pago (se ignora)
     */
    NotificacionPago resolver(String firma, String requestId, String dataId, String tipo) throws MiException;
}
