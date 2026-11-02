package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

import java.time.LocalDateTime;

public record StockResponseDTO(String id, String idProducto, String codigoProducto, String nombreProducto,
                               int cantidadActual, int stockMinimo, boolean bajoMinimo,
                               LocalDateTime fechaActualizacion) implements BaseResponseDTO {
}
