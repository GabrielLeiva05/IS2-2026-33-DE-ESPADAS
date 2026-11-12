package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;

/** Confirmación presencial del pedido por el preventista: medio de pago preferido por el cliente. */
public record ConfirmarPedidoRequestDTO(FormaDePago formaDePago) {
}
