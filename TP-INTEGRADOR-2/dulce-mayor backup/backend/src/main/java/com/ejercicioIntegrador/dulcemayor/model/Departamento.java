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

/** Reutilizada del Integrador 1 (Departamento), adaptada a BaseEntity. */
@Entity
@Table(name = "departamentos")
@Getter
@Setter
@NoArgsConstructor
public class Departamento extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String nombre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
}
