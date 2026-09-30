package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "ordenes_compra_proveedor")
public class OrdenCompraProveedor {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false)
    private double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrdenCompraProveedor estado;

    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_proveedor", nullable = false)
    private Proveedor proveedor;
}
