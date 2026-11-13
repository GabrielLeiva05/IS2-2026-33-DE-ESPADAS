package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;

import java.util.List;

/**
 * Producto tal como se muestra en el catalogo publico y en la app del preventista:
 * datos del producto, precio mayorista vigente y stock disponible.
 * Reutiliza la idea del ProductoCatalogoDTO del Integrador 1, pero sin exponer la entidad.
 */
public record ProductoCatalogoDTO(String id, String codigo, String nombre, String descripcion,
                                  Presentacion presentacion, int unidadesPorPresentacion,
                                  String idCategoria, String categoria, String idSubCategoria, String subCategoria,
                                  boolean enOferta, double precio, int stockDisponible, List<String> idsImagen) {
}
