package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Producto del catálogo con precio vigente y stock disponible (/api/catalogo). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoCatalogoDTO {

    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private int unidadesPorPresentacion;
    private String idCategoria;
    private String categoria;
    private String idSubCategoria;
    private String subCategoria;
    private boolean enOferta;
    private double precio;
    private int stockDisponible;
    private List<String> idsImagen;
    /** Precio convertido con la cotización vigente; null si no hay cotización. */
    private BigDecimal precioUsd;
    /** Precio con la mejor promoción porcentual vigente (calculado en el backend); null si no hay. */
    private BigDecimal precioPromocional;
    private BigDecimal precioPromocionalUsd;
    /** Textos de las promociones vigentes ("10% de descuento", "Desde 10 bultos: 5%"). */
    private List<String> promociones = List.of();

    /** "Caja x 24", "Pack x 12", "Unidad". */
    public String getPresentacionLegible() {
        if (presentacion == null) {
            return "";
        }
        String texto = presentacion.charAt(0) + presentacion.substring(1).toLowerCase(java.util.Locale.ROOT);
        return unidadesPorPresentacion > 1 ? texto + " x " + unidadesPorPresentacion : texto;
    }

    public String getPrimeraImagen() {
        return idsImagen == null || idsImagen.isEmpty() ? null : idsImagen.get(0);
    }
}
