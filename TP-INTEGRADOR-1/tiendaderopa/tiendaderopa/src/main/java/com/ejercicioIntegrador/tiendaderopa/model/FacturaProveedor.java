package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas_proveedor")
public class FacturaProveedor extends Factura{

    @ManyToOne
    private Proveedor proveedor;


    @Override
    public int getSignoMovimientoStock() {
        return 1; // llegó mercadería, suma
    }
}
