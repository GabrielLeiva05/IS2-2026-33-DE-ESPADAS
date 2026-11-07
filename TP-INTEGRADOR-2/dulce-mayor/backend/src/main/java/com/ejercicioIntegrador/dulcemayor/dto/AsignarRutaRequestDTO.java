package com.ejercicioIntegrador.dulcemayor.dto;

import java.time.LocalDate;
import java.util.List;

/** Asignación de pedidos a la jornada de reparto de un distribuidor. */
public record AsignarRutaRequestDTO(LocalDate fecha, String idDistribuidor, List<String> idsPedidos,
                                    String observacion) {
}
