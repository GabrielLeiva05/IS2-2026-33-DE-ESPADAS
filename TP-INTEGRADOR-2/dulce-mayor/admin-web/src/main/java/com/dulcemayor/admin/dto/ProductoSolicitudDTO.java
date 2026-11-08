package com.dulcemayor.admin.dto;

import java.util.List;

/** Cuerpo de POST/PUT /api/productos (ProductoRequestDTO del backend). */
public record ProductoSolicitudDTO(String codigo, String nombre, String descripcion, String presentacion,
                                   Integer unidadesPorPresentacion, boolean enOferta, String idSubCategoria,
                                   List<String> idsImagen) {
}
