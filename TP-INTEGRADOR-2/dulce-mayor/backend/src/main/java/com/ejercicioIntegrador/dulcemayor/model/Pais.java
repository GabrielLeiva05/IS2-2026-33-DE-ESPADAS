package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Reutilizada del Integrador 1 (Pais), adaptada a BaseEntity. */
@Entity
@Table(name = "paises")
@Getter
@Setter
@NoArgsConstructor
public class Pais extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String nombre;
}
