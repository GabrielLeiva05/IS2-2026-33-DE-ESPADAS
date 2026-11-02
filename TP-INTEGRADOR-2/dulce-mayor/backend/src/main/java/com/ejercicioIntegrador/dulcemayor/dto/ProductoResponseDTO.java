package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;

import java.util.List;

public record ProductoResponseDTO(String id, String codigo, String nombre, String descripcion,
                                  Presentacion presentacion, int unidadesPorPresentacion, boolean enOferta,
                                  String idSubCategoria, String nombreSubCategoria,
                                  String idCategoria, String nombreCategoria,
                                  List<ImagenResponseDTO> imagenes, boolean eliminado) implements BaseResponseDTO {
}
