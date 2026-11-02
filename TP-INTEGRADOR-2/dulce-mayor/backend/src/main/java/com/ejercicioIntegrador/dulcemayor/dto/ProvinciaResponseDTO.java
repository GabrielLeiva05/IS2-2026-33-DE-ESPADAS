package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record ProvinciaResponseDTO(String id, String nombre, String idPais, String nombrePais,
                                   boolean eliminado) implements BaseResponseDTO {
}
