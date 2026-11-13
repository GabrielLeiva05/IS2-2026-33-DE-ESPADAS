package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;

import java.time.LocalDate;
import java.util.List;

/** crearRuta(fecha, tipoRuta, idEmpleado, idsPuntoVenta) del diagrama. El orden inicial es el de la lista. */
public record RutaRequestDTO(LocalDate fecha, TipoRuta tipoRuta, String idEmpleado, List<String> idsPuntoVenta) {
}
