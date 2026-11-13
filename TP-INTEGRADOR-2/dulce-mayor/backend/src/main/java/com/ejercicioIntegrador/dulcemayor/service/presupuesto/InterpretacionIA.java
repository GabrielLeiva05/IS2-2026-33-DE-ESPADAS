package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que la IA entendió del mensaje. No trae precios ni productos del catálogo: solo intención y textos.
 *
 * @param presupuesto monto en pesos que el cliente quiere gastar (solo para RECOMENDAR), o null
 * @param categoria   rubro que mencionó para la recomendación ("golosinas", "chocolates"), o null
 * @param respuesta   texto breve para saludos o preguntas generales (nunca precios)
 */
public record InterpretacionIA(AccionPresupuesto accion, List<ItemSolicitado> items, BigDecimal presupuesto,
                               String categoria, String respuesta) {

    public InterpretacionIA {
        accion = accion == null ? AccionPresupuesto.OTRA : accion;
        items = items == null ? List.of() : List.copyOf(items);
    }
}
