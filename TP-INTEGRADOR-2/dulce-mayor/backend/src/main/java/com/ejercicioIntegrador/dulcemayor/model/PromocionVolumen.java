package com.ejercicioIntegrador.dulcemayor.model;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Descuento según la cantidad comprada de cada producto. Ejemplo: de 10 a 19 bultos 5 %, 20 o más 10 %
 * (las cantidades que no caen en ningún tramo pagan precio normal).
 */
@Entity
@Table(name = "promociones_volumen")
@Getter
@Setter
@NoArgsConstructor
public class PromocionVolumen extends Promocion {

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("cantidadDesde ASC")
    private List<TramoVolumen> tramos = new ArrayList<>();

    @Override
    public TipoPromocion getTipo() {
        return TipoPromocion.VOLUMEN;
    }

    public void reemplazarTramos(List<TramoVolumen> nuevos) {
        tramos.clear();
        for (TramoVolumen tramo : nuevos) {
            tramo.setPromocion(this);
            tramos.add(tramo);
        }
    }

    /** Porcentaje para esa cantidad (0 si no cae en ningún tramo). */
    public BigDecimal porcentajePara(int cantidad) {
        return tramos.stream().filter(t -> t.incluye(cantidad)).map(TramoVolumen::getPorcentaje)
                .findFirst().orElse(BigDecimal.ZERO);
    }
}
