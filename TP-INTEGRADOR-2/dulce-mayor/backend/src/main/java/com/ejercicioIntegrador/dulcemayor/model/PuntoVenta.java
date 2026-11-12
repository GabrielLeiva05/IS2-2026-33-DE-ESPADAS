package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.CondicionIva;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comercio del cliente (HU-17). Su dirección es la COMERCIAL, independiente de la dirección particular del cliente,
 * y siempre tiene coordenadas: son las que usan las rutas.
 * PuntoVenta 1..* ──> 1 Cliente; PuntoVenta 1 ──> 1 Direccion.
 */
@Entity
@Table(name = "puntos_venta")
@Getter
@Setter
@NoArgsConstructor
public class PuntoVenta extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombreFantasia;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 11)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CondicionIva condicionIva;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false, unique = true)
    private Direccion direccion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
