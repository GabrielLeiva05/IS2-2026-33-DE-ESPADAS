package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Factura de un proveedor (producto concreto del Factory Method), cargada por administración.
 * FacturaProveedor * ──> 1 Proveedor. El número es el del comprobante del proveedor: es único por proveedor.
 * No mueve stock: el ingreso de mercadería se registra en el módulo de stock (la regla no está definida en el backlog).
 */
@Entity
@Table(name = "facturas_proveedores")
@Getter
@Setter
@NoArgsConstructor
public class FacturaProveedor extends Factura {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    public FacturaProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
