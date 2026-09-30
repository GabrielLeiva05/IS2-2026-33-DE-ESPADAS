package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "facturas")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Factura {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @NotNull(message = "El número de factura es obligatorio")
    @Min(value = 1, message = "El número de factura debe ser mayor a 0")
    @Column(name = "numero_factura", unique = true, nullable = false)
    private Long numeroFactura;

    @SuppressWarnings("deprecation")
    @NotNull(message = "La fecha de factura es obligatoria")
    @Temporal(TemporalType.DATE)
    private Date fechaFactura;

    private double totalPagado;

    @NotNull(message = "El estado de la factura es obligatorio")
    @Enumerated(EnumType.STRING)
    private EstadoFactura estadoFactura;

    @NotNull(message = "El campo detalle es requerido")
    @OneToMany
    private java.util.List<DetalleFactura> detalleFactura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name= "fk_forma_de_pago", nullable = false)
    private FormaDePago formaDePago;

    @NotNull(message = "El campo eliminado no puede ser nulo")
    @Column(nullable = false)
    private boolean eliminado;

}
