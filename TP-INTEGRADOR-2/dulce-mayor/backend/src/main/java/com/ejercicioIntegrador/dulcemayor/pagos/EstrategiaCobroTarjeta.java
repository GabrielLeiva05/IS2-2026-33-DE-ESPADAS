package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;

/**
 * Capacidad de los medios que pueden cobrar con tarjeta sin salir de la aplicación
 * (Mercado Pago Checkout API). El navegador tokeniza la tarjeta y el servidor crea el pago.
 */
public interface EstrategiaCobroTarjeta {

    ResultadoTarjeta cobrarConTarjeta(Pago pago, ClienteMercadoPago.DatosTarjeta tarjeta) throws MiException;

    /** Resultado del cobro con tarjeta: identificador en el proveedor, estado y si quedó aprobado. */
    record ResultadoTarjeta(String idPagoExterno, String estado, boolean aprobado) {
    }
}
