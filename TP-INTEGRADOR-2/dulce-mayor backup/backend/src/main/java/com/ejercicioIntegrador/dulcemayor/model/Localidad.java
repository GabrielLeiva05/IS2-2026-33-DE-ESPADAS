package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Reutilizada del Integrador 1 (Localidad), adaptada a BaseEntity. */
@Entity
@Table(name = "localidades")
@Getter
@Setter
@NoArgsConstructor
public class Localidad extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 10)
    private String codigoPostal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;
}
