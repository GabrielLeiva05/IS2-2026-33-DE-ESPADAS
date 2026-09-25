package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas_cliente")
public class FacturaCliente extends Factura {

    public String getTipoFactura(){
        return "CLIENTE";
    }
}
