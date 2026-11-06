package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "puntos_venta")
@Getter
@Setter
@NoArgsConstructor
public class PuntoVenta extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombreComercial;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String emailContacto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false, unique = true)
    private Direccion direccion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preventista_id", nullable = false)
    private Preventista preventista;
}
