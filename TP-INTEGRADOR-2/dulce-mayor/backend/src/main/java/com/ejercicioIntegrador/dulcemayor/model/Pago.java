package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Intento de cobro del total de un pedido. Se persiste el estado del pago por separado del pedido
 * y los datos de cada proveedor (preferencia de Mercado Pago, número de transferencia, etc.).
 * referenciaOperacion es la clave de idempotencia interna; referenciaExterna identifica la
 * operación en el proveedor.
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO.setScale(2);

    @Column(nullable = false, unique = true, updatable = false, length = 40)
    private String referenciaOperacion;

    @Column(length = 255)
    private String referenciaExterna;

    /** Contenido EMVCo del QR dinámico de Mercado Pago (Checkout API), enviado por correo. */
    @Column(length = 1024)
    private String qrContenido;

    @Column(length = 200)
    private String observacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaAcreditacion;

    @Version
    private long version;

    @PrePersist
    void inicializar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (referenciaOperacion == null) {
            referenciaOperacion = UUID.randomUUID().toString();
        }
    }

    public boolean estaPendiente() {
        return estado == EstadoPago.PENDIENTE;
    }
}
