package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Código promocional. Un pedido admite un único código y no hay acumulación (regla acordada).
 * El descuento se calcula siempre en el servidor sobre el subtotal elegible.
 */
@Entity
@Table(name = "promociones")
@Getter
@Setter
@NoArgsConstructor
public class Promocion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPromocion tipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;

    @Column(nullable = false)
    private boolean activa = true;

    /** Cuando es true, el código no puede volver a usarse en otro pedido confirmado. */
    @Column(nullable = false)
    private boolean usoUnico;

    @Column(precision = 14, scale = 2)
    private BigDecimal minimoCompra;

    public boolean vigente(LocalDate fecha) {
        if (!activa || fechaDesde == null || fecha.isBefore(fechaDesde)) {
            return false;
        }
        return fechaHasta == null || !fecha.isAfter(fechaHasta);
    }
}
