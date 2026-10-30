package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity 
@Table(name = "facturas_clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacturaCliente extends Factura {

    @OneToMany(mappedBy = "facturaCliente")
    private List<OrdenCompra> ordenesCompra = new ArrayList<>();

}