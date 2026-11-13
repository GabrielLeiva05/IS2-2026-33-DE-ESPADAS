package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma una propuesta que entre en un monto con un algoritmo goloso: primero un bulto de cada producto (del más
 * barato al más caro, hasta {@link #VARIEDAD} productos) y después reparte de a un bulto entre los elegidos mientras
 * alcance el dinero y el stock. Usa el precio unitario ya rebajado por la promoción porcentual; las promociones por
 * volumen solo pueden bajar el total, así que la propuesta nunca supera el monto.
 */
public final class RecomendadorPresupuesto {

    static final int VARIEDAD = 6;
    static final int MAXIMO_POR_PRODUCTO = 50;

    /** Producto candidato con su precio unitario efectivo y los bultos disponibles. */
    public record Candidato(String idProducto, BigDecimal precioUnitario, int stock) {
    }

    private RecomendadorPresupuesto() {
    }

    /** @return idProducto → bultos, en el orden en que se eligieron (vacío si no entra ni un bulto) */
    public static Map<String, Integer> recomendar(List<Candidato> candidatos, BigDecimal monto) {
        Map<String, Integer> elegidos = new LinkedHashMap<>();
        if (monto == null || monto.signum() <= 0) {
            return elegidos;
        }
        List<Candidato> ordenados = new ArrayList<>(candidatos.stream()
                .filter(c -> c.stock() > 0 && c.precioUnitario() != null && c.precioUnitario().signum() > 0).toList());
        ordenados.sort(Comparator.comparing(Candidato::precioUnitario).thenComparing(Candidato::idProducto));
        BigDecimal restante = monto;
        List<Candidato> enPropuesta = new ArrayList<>();
        for (Candidato candidato : ordenados) {
            if (enPropuesta.size() == VARIEDAD) {
                break;
            }
            if (candidato.precioUnitario().compareTo(restante) <= 0) {
                elegidos.put(candidato.idProducto(), 1);
                enPropuesta.add(candidato);
                restante = restante.subtract(candidato.precioUnitario());
            }
        }
        boolean agrego = true;
        while (agrego) {
            agrego = false;
            for (Candidato candidato : enPropuesta) {
                int actual = elegidos.get(candidato.idProducto());
                if (actual < Math.min(candidato.stock(), MAXIMO_POR_PRODUCTO)
                        && candidato.precioUnitario().compareTo(restante) <= 0) {
                    elegidos.put(candidato.idProducto(), actual + 1);
                    restante = restante.subtract(candidato.precioUnitario());
                    agrego = true;
                }
            }
        }
        return elegidos;
    }
}
