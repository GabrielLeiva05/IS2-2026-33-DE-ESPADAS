package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Stock centralizado del almacen: un registro por producto (relacion 1 a 1 del diagrama). */
@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
public class Stock extends BaseEntity {

    @Column(nullable = false)
    private int cantidadActual;

    @Column(nullable = false)
    private int stockMinimo;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;
}
