package com.ejercicioIntegrador.tiendaderopa.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * ARCHIVO NUEVO. DTO real (no una entidad disfrazada): una línea que
 * viene del formulario "Generar Orden de Compra", antes de convertirse
 * en un DetalleOrdenCompraProveedor persistido. No tiene anotaciones JPA
 * a propósito — es un dato de transporte, no de persistencia.
 */
@Getter
@Setter
public class ItemCompraDTO {
    private String idProducto;
    private int cantidad;
    private double precioCompra;
}
