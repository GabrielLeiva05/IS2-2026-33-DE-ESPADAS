package com.ejercicioIntegrador.dulcemayor.eventos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo;

/**
 * Evento de dominio del pedido (patrón Observer con el bus de eventos de Spring): los servicios de
 * pedido, pago y ruta solo publican el hecho; el envío de correo queda desacoplado en
 * {@link EscuchaEventosPedido}.
 */
public record EventoPedido(String idPedido, TipoCorreo tipo, String destinatario, String nombreComercio,
                           String detalle) {
}
