package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Registro histórico de un descuento aplicado a un pedido. Copia el nombre, el código y el porcentaje de la
 * promoción en el momento de la carga: si la promoción se modifica o elimina después, el pedido no cambia.
 * detalle = null cuando el descuento es sobre el pedido completo (código de importe fijo).
 */
@Entity
@Table(name = "descuentos_pedido")
@Getter
@Setter
@NoArgsConstructor
public class DescuentoPedido extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_pedido_id")
    private DetallePedido detalle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoPromocion tipo;

    /** Id de la promoción (sin clave foránea: es una referencia histórica). */
    @Column(length = 36)
    private String idPromocion;

    @Column(nullable = false, length = 80)
    private String nombrePromocion;

    @Column(length = 30)
    private String codigo;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
}
