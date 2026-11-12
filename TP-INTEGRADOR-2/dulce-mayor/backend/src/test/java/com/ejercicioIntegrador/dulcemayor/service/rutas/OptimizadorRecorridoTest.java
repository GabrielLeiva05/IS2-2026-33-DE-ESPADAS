package com.ejercicioIntegrador.dulcemayor.service.rutas;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El optimizador: exacto hasta 12 paradas (se compara con fuerza bruta) y heurístico con más. */
class OptimizadorRecorridoTest {

    private static double[][] matrizAleatoria(Random azar, int puntos, boolean simetrica) {
        double[][] c = new double[puntos][puntos];
        for (int i = 0; i < puntos; i++) {
            for (int j = 0; j < puntos; j++) {
                if (i != j) {
                    c[i][j] = simetrica && j < i ? c[j][i] : 30 + azar.nextInt(1500);
                }
            }
        }
        return c;
    }

    /** Mejor costo probando todas las permutaciones (solo para matrices chicas). */
    private static double fuerzaBruta(double[][] c) {
        List<Integer> destinos = new ArrayList<>();
        for (int i = 1; i < c.length; i++) {
            destinos.add(i);
        }
        return permutar(c, new ArrayList<>(), destinos);
    }

    private static double permutar(double[][] c, List<Integer> elegidos, List<Integer> restantes) {
        if (restantes.isEmpty()) {
            return OptimizadorRecorrido.costo(c, elegidos);
        }
        double mejor = Double.POSITIVE_INFINITY;
        for (int i = 0; i < restantes.size(); i++) {
            List<Integer> e = new ArrayList<>(elegidos);
            e.add(restantes.get(i));
            List<Integer> r = new ArrayList<>(restantes);
            r.remove(i);
            mejor = Math.min(mejor, permutar(c, e, r));
        }
        return mejor;
    }

    private static boolean esPermutacion(List<Integer> orden, int destinos) {
        return orden.size() == destinos && new HashSet<>(orden).size() == destinos
                && orden.stream().allMatch(i -> i >= 1 && i <= destinos);
    }

    @Test
    void hastaElLimiteDevuelveElOptimoAunConMatricesAsimetricas() {
        Random azar = new Random(20261106L);
        for (int caso = 0; caso < 60; caso++) {
            int destinos = 1 + caso % 8;
            double[][] c = matrizAleatoria(azar, destinos + 1, caso % 2 == 0);
            OptimizadorRecorrido.Resultado r = OptimizadorRecorrido.optimizar(c);
            assertTrue(r.exacto());
            assertTrue(esPermutacion(r.orden(), destinos));
            assertEquals(fuerzaBruta(c), r.costo(), 1e-6);
            assertEquals(OptimizadorRecorrido.costo(c, r.orden()), r.costo(), 1e-6);
        }
    }

    @Test
    void mejoraAlVecinoMasCercanoCuandoLaPrimeraEleccionEsUnaTrampa() {
        // Desde 0 lo más cercano es 1, pero de 1 a los demás es caro; de 3 a 1 se llega rápido (mano única)
        double[][] c = {
                {0, 1, 5, 6},
                {1, 0, 50, 50},
                {5, 50, 0, 1},
                {6, 2, 1, 0}};
        assertEquals(List.of(1, 2, 3), OptimizadorRecorrido.vecinoMasCercano(c));     // 1 + 50 + 1 = 52
        OptimizadorRecorrido.Resultado r = OptimizadorRecorrido.optimizar(c);
        assertEquals(List.of(2, 3, 1), r.orden());                                     // 5 + 1 + 2 = 8
        assertEquals(8.0, r.costo(), 1e-9);
        // El 2-opt es una búsqueda local: acá no sale del orden del vecino más cercano (ninguna inversión de tramo
        // mejora). Por eso, mientras se pueda (hasta 12 paradas), se usa la búsqueda exacta.
        assertEquals(List.of(1, 2, 3), OptimizadorRecorrido.mejorar2Opt(c, List.of(1, 2, 3)));
    }

    @Test
    void conMasParadasUsaLaHeuristicaYNoEmpeoraAlVecinoMasCercano() {
        Random azar = new Random(7L);
        for (int caso = 0; caso < 10; caso++) {
            int destinos = 13 + caso * 4;
            double[][] c = matrizAleatoria(azar, destinos + 1, caso % 2 == 1);
            OptimizadorRecorrido.Resultado r = OptimizadorRecorrido.optimizar(c);
            assertFalse(r.exacto());
            assertTrue(esPermutacion(r.orden(), destinos));
            double vecino = OptimizadorRecorrido.costo(c, OptimizadorRecorrido.vecinoMasCercano(c));
            assertTrue(r.costo() <= vecino + 1e-9);
        }
    }

    @Test
    void sinPendientesNoHayOrden() {
        assertTrue(OptimizadorRecorrido.optimizar(new double[][]{{0}}).orden().isEmpty());
    }
}
