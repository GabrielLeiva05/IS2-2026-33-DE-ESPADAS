package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Pago de un pedido (diagrama, bloque Facturación y pagos).
 * Pago 1 ──> 1 Pedido (la columna pedido_id es única): un pedido tiene a lo sumo un pago.
 *
 * - EFECTIVO y TRANSFERENCIA quedan PENDIENTE hasta que se cobren (el cobro en efectivo lo registra el
 *   distribuidor al entregar; no hay confirmación automática).
 * - QR: se genera una cobranza dinámica con la Checkout API de Mercado Pago (sin redirección del cliente).
 *   El webhook de Mercado Pago consulta el estado y, cuando se aprueba, marca el pago APROBADO y la factura PAGADA.
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaDePago formaDePago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(nullable = false)
    private double monto;

    /** Alias bancario informado al cliente cuando el medio de pago es TRANSFERENCIA. */
    @Column(length = 120)
    private String aliasTransferencia;

    /** Identificador de la orden (QR) devuelto por Mercado Pago. */
    @Column(length = 150)
    private String idOrdenMp;

    /** Contenido que se codifica en el QR (qr_data de Mercado Pago). */
    @Column(length = 2000)
    private String qrData;

    /** Identificador del pago de Mercado Pago cuando se acredita. */
    @Column(length = 150)
    private String idPagoMp;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAprobacion;
}
