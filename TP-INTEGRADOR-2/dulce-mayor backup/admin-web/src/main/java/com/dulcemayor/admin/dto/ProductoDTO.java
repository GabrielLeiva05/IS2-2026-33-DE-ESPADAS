package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Producto tal como lo devuelve GET /api/productos (ABM de productos). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDTO {

    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private int unidadesPorPresentacion;
    private boolean enOferta;
    private String idSubCategoria;
    private String nombreSubCategoria;
    private String idCategoria;
    private String nombreCategoria;
    private List<ImagenProductoDTO> imagenes = new ArrayList<>();
    private boolean eliminado;

    /** "CAJA" → "Caja x 24". */
    public String getPresentacionLegible() {
        if (presentacion == null) {
            return "";
        }
        String texto = presentacion.charAt(0) + presentacion.substring(1).toLowerCase();
        return unidadesPorPresentacion > 1 ? texto + " x " + unidadesPorPresentacion : texto;
    }
}
