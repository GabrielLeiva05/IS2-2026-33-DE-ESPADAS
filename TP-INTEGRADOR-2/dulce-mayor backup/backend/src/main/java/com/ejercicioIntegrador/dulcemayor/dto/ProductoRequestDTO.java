package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;

import java.util.List;

/**
 * Datos de entrada para crear o modificar un producto.
 * En la modificacion el codigo no puede cambiar e idsImagen es opcional (si viene, reemplaza las imagenes).
 */
public record ProductoRequestDTO(String codigo, String nombre, String descripcion, Presentacion presentacion,
                                 Integer unidadesPorPresentacion, boolean enOferta, String idSubCategoria,
                                 List<String> idsImagen) {
}
