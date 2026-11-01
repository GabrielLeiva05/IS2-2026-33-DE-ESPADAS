package com.ejercicioIntegrador.dulcemayor.dto;

/** Filtros opcionales del catalogo publico. Todos pueden venir vacios. */
public record FiltroCatalogoDTO(String idCategoria, String idSubCategoria, String texto, boolean soloOfertas) {
}
