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

import java.time.LocalDate;

@Entity
@Table(name = "vigencias_precio")
@Getter
@Setter
@NoArgsConstructor
public class VigenciaPrecio extends BaseEntity {

    @Column(nullable = false)
    private LocalDate fechaDesde;

    /** Nula mientras la vigencia este abierta (ver nota del diagrama). */
    private LocalDate fechaHasta;

    @Column(nullable = false)
    private double precio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
