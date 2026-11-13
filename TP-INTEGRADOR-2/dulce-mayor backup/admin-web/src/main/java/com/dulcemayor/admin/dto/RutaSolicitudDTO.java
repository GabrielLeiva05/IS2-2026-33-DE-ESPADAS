package com.dulcemayor.admin.dto;

import java.util.List;

/** Cuerpo de POST /api/rutas (RutaRequestDTO del backend). */
public record RutaSolicitudDTO(String fecha, String tipoRuta, String idEmpleado, List<String> idsPuntoVenta) {
}
