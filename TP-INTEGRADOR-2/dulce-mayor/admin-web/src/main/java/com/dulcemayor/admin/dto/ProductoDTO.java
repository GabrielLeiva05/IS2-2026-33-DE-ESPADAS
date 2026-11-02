package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Producto administrable (/api/productos), con o sin precio vigente. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDTO {

    private String id;
    private String codigo;
    private String nombre;
    private String nombreCategoria;
    private String nombreSubCategoria;
    private boolean enOferta;
    private boolean eliminado;
}
