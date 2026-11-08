package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Factura al cliente (producto concreto del Factory Method). FacturaCliente 1 ──> 1 Pedido (diagrama):
 * la columna pedido_id es única, así un pedido nunca tiene dos facturas.
 * El cliente se obtiene por el pedido (Pedido → PuntoVenta → Cliente), como en el diagrama.
 * codigoQrPago queda vacío: lo completará el módulo de Pagos (billetera virtual, HU-29).
 */
@Entity
@Table(name = "facturas_clientes")
@Getter
@Setter
@NoArgsConstructor
public class FacturaCliente extends Factura {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(length = 500)
    private String codigoQrPago;

    public FacturaCliente(Pedido pedido) {
        this.pedido = pedido;
    }

    @Override
    public double getDescuento() {
        return pedido == null ? 0 : pedido.getDescuento();
    }
}
