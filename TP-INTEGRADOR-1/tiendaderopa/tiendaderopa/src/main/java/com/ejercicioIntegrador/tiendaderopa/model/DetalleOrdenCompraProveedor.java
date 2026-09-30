package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * ARCHIVO NUEVO. Una línea de una OrdenCompraProveedor: qué producto,
 * cuánta cantidad, y a qué precio de costo se le compró al proveedor.
 *
 * precioCompra es justamente el campo que NO existe en DetalleCompra
 * (el del carrito de cliente) — es la principal razón por la que se
 * armó una clase de detalle propia en vez de reusar esa.
 */
@Entity
@Getter
@Setter
@Table(name = "detalle_orden_compra_proveedor")
public class DetalleOrdenCompraProveedor {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private double precioCompra;

    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_orden_compra_proveedor", nullable = false)
    private OrdenCompraProveedor ordenCompraProveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_producto", nullable = false)
    private Producto producto;
}