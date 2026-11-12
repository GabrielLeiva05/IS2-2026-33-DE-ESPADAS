package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ruta del día de un empleado: VISITA (preventista) o ENTREGA (distribuidor). Ruta * ──> 1 Empleado.
 * Una sola ruta por empleado, fecha y tipo.
 */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta extends BaseEntity {

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRuta tipoRuta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ElementCollection
    @CollectionTable(name = "ruta_puntos", joinColumns = @JoinColumn(name = "ruta_id"))
    @OrderBy("orden ASC")
    private List<PuntoRuta> puntos = new ArrayList<>();

    /**
     * Cómo se decidió el orden actual de los pendientes (por ejemplo "óptimo por tiempo de manejo (OSRM)").
     * Null mientras el orden sea el que armó administración o el de alta de la ruta de entregas.
     */
    @Column(length = 200)
    private String criterioOrden;
}
