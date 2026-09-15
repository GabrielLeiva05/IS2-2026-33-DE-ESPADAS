package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoSucursal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "empresa")
public class Empresa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private String id;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSucursal tipoSucursal;

    @Column(nullable = false)
    private boolean eliminado = false;

    public Empresa(String razonSocial, String cuit, TipoSucursal tipoSucursal) {
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.tipoSucursal = tipoSucursal;
    }
}