package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @Column(name = "razon-social")
    private String razonSocial;
}
