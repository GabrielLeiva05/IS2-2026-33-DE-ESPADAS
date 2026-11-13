package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecomendadorPresupuestoTest {

    private static RecomendadorPresupuesto.Candidato c(String id, String precio, int stock) {
        return new RecomendadorPresupuesto.Candidato(id, new BigDecimal(precio), stock);
    }

    private static BigDecimal total(Map<String, Integer> propuesta, List<RecomendadorPresupuesto.Candidato> candidatos) {
        BigDecimal total = BigDecimal.ZERO;
        for (RecomendadorPresupuesto.Candidato candidato : candidatos) {
            total = total.add(candidato.precioUnitario().multiply(BigDecimal.valueOf(propuesta.getOrDefault(candidato.idProducto(), 0))));
        }
        return total;
    }

    @Test
    void reparteVariedadSinPasarseDelMonto() {
        List<RecomendadorPresupuesto.Candidato> candidatos = List.of(c("a", "1000", 10), c("b", "2500", 10), c("c", "400", 10));
        Map<String, Integer> propuesta = RecomendadorPresupuesto.recomendar(candidatos, new BigDecimal("10000"));
        assertEquals(3, propuesta.size());
        BigDecimal total = total(propuesta, candidatos);
        assertTrue(total.compareTo(new BigDecimal("10000")) <= 0);
        // No sobra para otro bulto del más barato
        assertTrue(new BigDecimal("10000").subtract(total).compareTo(new BigDecimal("400")) < 0, "sobra " + total);
    }

    @Test
    void respetaElStock() {
        Map<String, Integer> propuesta = RecomendadorPresupuesto.recomendar(List.of(c("a", "100", 3)), new BigDecimal("100000"));
        assertEquals(Map.of("a", 3), propuesta);
    }

    @Test
    void sinMontoOSinAlcanceNoPropone() {
        assertTrue(RecomendadorPresupuesto.recomendar(List.of(c("a", "5000", 3)), new BigDecimal("1000")).isEmpty());
        assertTrue(RecomendadorPresupuesto.recomendar(List.of(c("a", "5000", 3)), null).isEmpty());
        assertTrue(RecomendadorPresupuesto.recomendar(List.of(c("a", "5000", 0)), new BigDecimal("99999")).isEmpty());
    }
}
