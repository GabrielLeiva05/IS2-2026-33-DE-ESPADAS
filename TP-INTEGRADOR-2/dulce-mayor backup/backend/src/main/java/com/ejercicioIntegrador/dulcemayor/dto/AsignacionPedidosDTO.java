package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/** asignarDistribuidor(id, idDistribuidor) del diagrama, para varios pedidos a la vez. */
public record AsignacionPedidosDTO(String idDistribuidor, List<String> idsPedido) {
}
