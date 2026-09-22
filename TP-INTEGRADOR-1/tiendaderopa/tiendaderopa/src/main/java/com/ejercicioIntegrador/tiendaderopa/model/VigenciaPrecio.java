package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VigenciaPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private LocalDate fechaDesde;

    @Temporal(TemporalType.DATE)
    private LocalDate fechaHasta;

    //@Column
    private double precio;

    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne
    private Producto producto;
}
