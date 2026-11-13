package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;

import java.util.List;

/**
 * Producto tal como se muestra en el catalogo publico y en la app del preventista:
 * datos del producto, precio mayorista vigente y stock disponible.
 * precioPromocional: precio por unidad con la mejor promoción automática vigente (null si no hay); las
 * promociones por volumen se informan en "promociones" porque dependen de la cantidad.
 * precioUsd: precio convertido con la cotización vigente (null si no hay cotización disponible).
 * Reutiliza la idea del ProductoCatalogoDTO del Integrador 1, pero sin exponer la entidad.
 */
public record ProductoCatalogoDTO(String id, String codigo, String nombre, String descripcion,
                                  Presentacion presentacion, int unidadesPorPresentacion,
                                  String idCategoria, String categoria, String idSubCategoria, String subCategoria,
                                  boolean enOferta, double precio, int stockDisponible, List<String> idsImagen,
                                  BigDecimal precioUsd, BigDecimal precioPromocional, BigDecimal precioPromocionalUsd,
                                  List<String> promociones) {
}
