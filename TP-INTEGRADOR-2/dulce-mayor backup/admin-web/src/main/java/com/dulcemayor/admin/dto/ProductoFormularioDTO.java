package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de alta y modificación de productos. En el alta también se cargan el precio inicial y el stock
 * (usando los endpoints de vigencias de precio y stock del módulo de Catálogo y Stock, sin un CRUD aparte).
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductoFormularioDTO {

    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String presentacion = "CAJA";
    private Integer unidadesPorPresentacion = 1;
    private boolean enOferta;
    private String idCategoria;
    private String idSubCategoria;
    private List<String> idsImagen = new ArrayList<>();
    // Solo en el alta
    private Double precio;
    private Integer stockInicial;
    private Integer stockMinimo;

    public boolean isNuevo() {
        return id == null || id.isBlank();
    }

    public static ProductoFormularioDTO desde(ProductoDTO p) {
        ProductoFormularioDTO f = new ProductoFormularioDTO();
        f.setId(p.getId());
        f.setCodigo(p.getCodigo());
        f.setNombre(p.getNombre());
        f.setDescripcion(p.getDescripcion());
        f.setPresentacion(p.getPresentacion());
        f.setUnidadesPorPresentacion(p.getUnidadesPorPresentacion());
        f.setEnOferta(p.isEnOferta());
        f.setIdCategoria(p.getIdCategoria());
        f.setIdSubCategoria(p.getIdSubCategoria());
        f.setIdsImagen(new ArrayList<>(p.getImagenes().stream().map(ImagenProductoDTO::getId).toList()));
        return f;
    }
}
