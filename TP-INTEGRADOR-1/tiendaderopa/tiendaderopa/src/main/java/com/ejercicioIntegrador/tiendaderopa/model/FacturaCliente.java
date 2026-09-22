package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table (name = "facturas_clientes")
public class FacturaCliente extends Factura {

    @Override
    public int getSignoMovimientoStock() {
        return -1; // se vendió, resta
    }
}
