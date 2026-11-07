package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sucursal de atención al público que se muestra en el sitio institucional. */
@Entity
@Table(name = "sucursales")
@Getter
@Setter
@NoArgsConstructor
public class Sucursal extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String emailContacto;

    @Column(length = 120)
    private String horario;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false, unique = true)
    private Direccion direccion;

    @Column(nullable = false)
    private boolean activa = true;
}
