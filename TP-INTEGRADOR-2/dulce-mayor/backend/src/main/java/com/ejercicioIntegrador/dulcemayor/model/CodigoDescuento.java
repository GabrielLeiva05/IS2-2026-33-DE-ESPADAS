package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoValorDescuento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Código de descuento (por ejemplo DULCE10) que el preventista ingresa al cargar el pedido.
 * Sin productos ni subcategorías se aplica al pedido completo.
 * acumulable = false: no se aplica sobre los renglones que ya tienen una promoción automática.
 */
@Entity
@Table(name = "codigos_descuento")
@Getter
@Setter
@NoArgsConstructor
public class CodigoDescuento extends Promocion {

    /** En mayúsculas. Único entre los códigos no eliminados (lo controla el servicio). */
    @Column(nullable = false, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoValorDescuento tipoValor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoMinimo;

    private Integer usosMaximos;

    @Column(nullable = false)
    private int usosRealizados;

    @Column(nullable = false)
    private boolean acumulable = true;

    @Override
    public TipoPromocion getTipo() {
        return TipoPromocion.CODIGO;
    }

    public boolean usosAgotados() {
        return usosMaximos != null && usosRealizados >= usosMaximos;
    }
}
