package com.ejercicioIntegrador.tiendaderopa.model;

public class FacturaCliente extends Factura {

    @Override
    public int getSignoMovimientoStock() {
        return -1; // se vendió, resta
    }
}
