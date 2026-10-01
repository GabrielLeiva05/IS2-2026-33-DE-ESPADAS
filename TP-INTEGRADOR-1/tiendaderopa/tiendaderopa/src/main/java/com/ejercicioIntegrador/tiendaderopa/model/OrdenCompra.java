package com.ejercicioIntegrador.tiendaderopa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;

@Entity
@Table(name = "ordenes_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 100)
    private String identificadorCompra;

    @SuppressWarnings("deprecation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private double total;

    @Enumerated(EnumType.STRING)
    private EstadoOrdenCompra estadoOrdenCompra;

    @Column(nullable = false)
    private boolean eliminado = false;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_cliente_id")
    private FacturaCliente facturaCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Column(name = "mercado_pago_preference_id", length = 100)
    private String mercadoPagoPreferenceId;

    @Column(name = "mercado_pago_init_point", length = 1000)
    private String mercadoPagoInitPoint;

    @Column(name = "mercado_pago_payment_id", length = 80)
    private String mercadoPagoPaymentId;

    @Column(name = "mercado_pago_status", length = 40)
    private String mercadoPagoStatus;
    
}