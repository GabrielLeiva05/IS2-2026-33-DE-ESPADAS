package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "facturas_proveedor")
public class FacturaProveedor extends Factura{

    @ManyToOne
    private Proveedor proveedor;


    public String getTipoFactura(){
        return "PROVEEDOR";
    }
}
