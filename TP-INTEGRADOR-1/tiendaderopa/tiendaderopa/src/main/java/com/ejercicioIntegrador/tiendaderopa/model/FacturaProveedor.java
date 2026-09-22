package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas_proveedor")
public class FacturaProveedor extends Factura{

    @ManyToOne
    private Proveedor proveedor;


    public String getTipoFactura(){
        return "PROVEEDOR";
    }
}
