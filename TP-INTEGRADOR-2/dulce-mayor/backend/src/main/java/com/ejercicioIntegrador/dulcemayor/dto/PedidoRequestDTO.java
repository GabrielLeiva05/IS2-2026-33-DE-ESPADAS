package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

public record PedidoRequestDTO(String idPuntoVenta, List<PedidoLineaRequestDTO> lineas) {
}
