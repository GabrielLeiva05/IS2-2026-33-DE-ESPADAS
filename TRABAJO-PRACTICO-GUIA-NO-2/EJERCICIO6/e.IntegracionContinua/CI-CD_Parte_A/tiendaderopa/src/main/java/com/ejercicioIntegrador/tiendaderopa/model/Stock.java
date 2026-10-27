package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private int cantActual;

    private String observacion;
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_detalle_factura", nullable = false)
    private DetalleFactura detalleFactura;

    @CreationTimestamp
    private LocalDateTime fechaMovimiento;
}
