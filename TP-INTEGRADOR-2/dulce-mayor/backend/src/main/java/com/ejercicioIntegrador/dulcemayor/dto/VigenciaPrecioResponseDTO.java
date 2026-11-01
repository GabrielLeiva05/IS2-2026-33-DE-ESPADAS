package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

import java.time.LocalDate;

public record VigenciaPrecioResponseDTO(String id, String idProducto, String nombreProducto,
                                        LocalDate fechaDesde, LocalDate fechaHasta, double precio,
                                        boolean vigente, boolean eliminado) implements BaseResponseDTO {
}
