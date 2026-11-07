package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoParada;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Parada de la hoja de ruta: un pedido a entregar, con su resultado. */
@Entity
@Table(name = "paradas")
@Getter
@Setter
@NoArgsConstructor
public class Parada extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private int orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoParada estado = EstadoParada.PENDIENTE;

    @Column(length = 200)
    private String motivoFallo;

    @Column(length = 200)
    private String observacion;

    private LocalDateTime fechaResolucion;

    @Column(nullable = false)
    private boolean cobroEfectivo;

    @Version
    private long version;
}
