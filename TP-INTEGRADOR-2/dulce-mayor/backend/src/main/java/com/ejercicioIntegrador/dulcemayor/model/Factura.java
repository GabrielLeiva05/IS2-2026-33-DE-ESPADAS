package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factura (abstracta) del diagrama, bloque Facturación y pagos. Es el PRODUCTO del patrón Factory Method:
 * sus productos concretos son FacturaCliente y FacturaProveedor, y los crean CreadorConcretoFacturaCliente y
 * CreadorConcretoFacturaProveedor (ver service/facturacion).
 *
 * Factura ◆── 1..* DetalleFactura. Herencia JOINED: tabla "facturas" con lo común y una tabla por subclase.
 * Importes: el diagrama guarda solo el total (con IVA). El subtotal sin IVA es la suma de los renglones y el IVA
 * es total − (subtotal − descuento); así nunca quedan dos valores que puedan contradecirse. Esos importes son
 * métodos calculados (el acceso de JPA es por atributos, así que no se guardan como columnas).
 * Relación con Pagos (pendiente): Factura 1 ──> * Pago y Factura * ──> 0..1 FormaDePago se agregarán con ese módulo.
 */
@Entity
@Table(name = "facturas")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Factura extends BaseEntity {

    @Column(nullable = false)
    private long numeroFactura;

    @Column(nullable = false)
    private LocalDateTime fechaFactura;

    /** Total con IVA. */
    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetalleFactura> detalles = new ArrayList<>();

    /** crearDetalleFactura(idProducto, cantidad, precioUnitario) del diagrama: agrega el renglón a la composición. */
    public DetalleFactura agregarDetalle(Producto producto, int cantidad, double precioUnitario) {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(Math.round(cantidad * precioUnitario * 100.0) / 100.0);
        detalle.setFactura(this);
        detalle.setEliminado(false);
        detalles.add(detalle);
        return detalle;
    }

    /** Subtotal sin IVA: suma de los renglones. */
    public double getSubtotal() {
        return Math.round(detalles.stream().mapToDouble(DetalleFactura::getSubtotal).sum() * 100.0) / 100.0;
    }

    /** Descuento aplicado antes del IVA (la factura de cliente lo toma del pedido). */
    public double getDescuento() {
        return 0;
    }

    /** IVA discriminado: total − (subtotal − descuento). */
    public double getIva() {
        return Math.round((total - (getSubtotal() - getDescuento())) * 100.0) / 100.0;
    }
}
