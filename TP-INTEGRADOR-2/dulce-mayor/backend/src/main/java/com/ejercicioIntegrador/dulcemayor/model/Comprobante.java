package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprobante interno no fiscal, emitido una sola vez por pedido cuando el pago total queda
 * acreditado. No reemplaza a la factura.
 */
@Entity
@Table(name = "comprobantes", uniqueConstraints = @UniqueConstraint(columnNames = "pedido_id"))
@Getter
@Setter
@NoArgsConstructor
public class Comprobante extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false, unique = true)
    private Pago pago;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO.setScale(2);

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @PrePersist
    void inicializar() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }
}
