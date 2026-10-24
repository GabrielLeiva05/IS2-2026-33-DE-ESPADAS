package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "facturas_proveedor")
public class FacturaProveedor extends Factura{

    @ManyToOne
    @JoinColumn(name = "fk-proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "fk-orden-compra-proveedor", nullable = false)
    private OrdenCompraProveedor ordenCompraProveedor;
}
