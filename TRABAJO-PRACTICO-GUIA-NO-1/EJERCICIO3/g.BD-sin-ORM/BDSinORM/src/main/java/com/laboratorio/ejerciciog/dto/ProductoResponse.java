package com.laboratorio.ejerciciog.dto;

import com.laboratorio.ejerciciog.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ProductoResponse {
    private  Integer codigo;
    private String nombre;
    private double precio;

    public ProductoResponse(Producto producto) {
        this.codigo = producto.getCodigo();
        this.nombre = producto.getNombre();
        this.precio = producto.getPrecio();
    }
}