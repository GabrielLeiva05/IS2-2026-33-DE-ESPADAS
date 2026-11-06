package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;

/** Inicio de un cobro: el medio lo elige quien opera el pedido. */
public record PagoRequestDTO(MedioPago medio) {
}
