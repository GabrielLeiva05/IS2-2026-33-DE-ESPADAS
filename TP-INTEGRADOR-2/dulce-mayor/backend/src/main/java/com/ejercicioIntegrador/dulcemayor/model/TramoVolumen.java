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

import java.math.BigDecimal;

/** Tramo de una promoción por volumen: de cantidadDesde a cantidadHasta (null = en adelante), porcentaje. */
@Entity
@Table(name = "tramos_volumen")
@Getter
@Setter
@NoArgsConstructor
public class TramoVolumen extends BaseEntity {

    @Column(nullable = false)
    private int cantidadDesde;

    private Integer cantidadHasta;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id", nullable = false)
    private PromocionVolumen promocion;

    public TramoVolumen(int cantidadDesde, Integer cantidadHasta, BigDecimal porcentaje) {
        this.cantidadDesde = cantidadDesde;
        this.cantidadHasta = cantidadHasta;
        this.porcentaje = porcentaje;
    }

    public boolean incluye(int cantidad) {
        return cantidad >= cantidadDesde && (cantidadHasta == null || cantidad <= cantidadHasta);
    }
}
