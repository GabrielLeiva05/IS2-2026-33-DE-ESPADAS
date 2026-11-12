package com.dulcemayor.admin.dto;

import java.util.List;

/** Cuerpo de POST /api/pedidos/asignacion. */
public record AsignacionSolicitudDTO(String idDistribuidor, List<String> idsPedido) {
}
