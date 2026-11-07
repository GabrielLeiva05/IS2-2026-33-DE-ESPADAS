package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Parada de la planificación diaria del preventista: qué comercio visita y en qué orden.
 * El orden inicial se sugiere por cercanía en línea recta y se puede reordenar a mano.
 */
@Entity
@Table(name = "visitas", uniqueConstraints = @UniqueConstraint(columnNames = {"preventista_id", "fecha", "punto_venta_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Visita extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preventista_id", nullable = false)
    private Preventista preventista;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    @Column(nullable = false)
    private int orden;

    @Column(length = 200)
    private String observacion;

    @Version
    private long version;
}
