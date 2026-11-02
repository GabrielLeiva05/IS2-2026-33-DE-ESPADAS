package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;

public record DepartamentoResponseDTO(String id, String nombre, String idProvincia, String nombreProvincia,
                                      boolean eliminado) implements BaseResponseDTO {
}
