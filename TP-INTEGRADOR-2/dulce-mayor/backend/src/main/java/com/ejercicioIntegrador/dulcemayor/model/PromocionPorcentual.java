package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Descuento porcentual sobre productos o subcategorías (por ejemplo, 15 % en alfajores). */
@Entity
@Table(name = "promociones_porcentuales")
@Getter
@Setter
@NoArgsConstructor
public class PromocionPorcentual extends Promocion {

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Override
    public TipoPromocion getTipo() {
        return TipoPromocion.PORCENTAJE_PRODUCTO;
    }
}
