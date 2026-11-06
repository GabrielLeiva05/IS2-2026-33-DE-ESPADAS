package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;

import java.time.LocalDate;
import java.util.List;

public record RutaResponseDTO(String id, LocalDate fecha, TipoRuta tipoRuta, String idEmpleado, String nombreEmpleado,
                              List<PuntoRutaDTO> puntos, int pendientes, int visitados,
                              boolean eliminado) implements BaseResponseDTO {
}
