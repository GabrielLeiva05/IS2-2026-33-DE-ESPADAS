package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Se usa "razon_social" (con guion bajo): un guion medio en el nombre de columna rompe el SQL en MySQL.
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private boolean eliminado = false;

    public Proveedor(String razonSocial) {
        this.razonSocial = razonSocial;
    }
}
