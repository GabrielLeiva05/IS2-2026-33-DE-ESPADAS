package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido tomado por un preventista en un punto de venta (HU-21).
 * Pedido 1 ◆── 1..* DetallePedido; * ──> 1 PuntoVenta; * ──> 1 Preventista; * ──> 0..1 Distribuidor.
 * fechaFacturacion registra la confirmación del cliente (todavía no hay módulo de facturación).
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido extends BaseEntity {

    @Column(nullable = false, unique = true)
    private long numeroPedido;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCarga;

    private LocalDateTime fechaFacturacion;

    private LocalDateTime fechaDespacho;

    private LocalDateTime fechaEntrega;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double descuento;

    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoPedido estado;

    /** Medio de pago preferido por el cliente, elegido por el preventista al confirmar (Ola de Pagos). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FormaDePago formaDePago;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_venta_id", nullable = false)
    private PuntoVenta puntoVenta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preventista_id", nullable = false)
    private Preventista preventista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribuidor_id")
    private Distribuidor distribuidor;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Descuentos aplicados al cargar el pedido (promociones y código), con copia de sus datos. */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DescuentoPedido> descuentos = new ArrayList<>();

    public void agregarDescuento(DescuentoPedido descuento) {
        descuento.setPedido(this);
        descuentos.add(descuento);
    }

    /** Código de descuento usado en el pedido (null si no se usó). */
    public String getCodigoDescuento() {
        return descuentos.stream().map(DescuentoPedido::getCodigo).filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    public boolean isConfirmado() {
        return fechaFacturacion != null;
    }
}
