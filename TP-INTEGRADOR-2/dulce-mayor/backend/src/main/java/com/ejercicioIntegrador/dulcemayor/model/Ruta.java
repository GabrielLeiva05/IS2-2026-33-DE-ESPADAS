package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoRuta;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Jornada de reparto de un distribuidor: agrupa las paradas (pedidos) de una fecha. */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta extends BaseEntity {

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "distribuidor_id", nullable = false)
    private Distribuidor distribuidor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    @Column(length = 200)
    private String observacion;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Parada> paradas = new ArrayList<>();

    @Version
    private long version;
}
