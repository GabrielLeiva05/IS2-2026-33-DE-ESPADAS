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

/**
 * Proveedor (diagrama, bloque Facturación y pagos): razón social, CUIT y dirección (Proveedor 1 ──> 1 Direccion).
 * Se implementa lo necesario para las facturas de proveedor. La relación con Contacto (1..*) del diagrama queda
 * pendiente: el proyecto todavía no tiene el módulo de contactos.
 */
@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 11)
    private String cuit;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id", nullable = false, unique = true)
    private Direccion direccion;
}
