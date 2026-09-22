package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
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
    private Proveedor proveedor;

}
