package com.ejercicioIntegrador.dulcemayor.service.rutas;

import java.util.ArrayList;
import java.util.List;

/**
 * Orden de visita de los puntos pendientes partiendo de la ubicación actual (índice 0 de la matriz), sin volver
 * al inicio (recorrido abierto: el preventista o el distribuidor termina en el último comercio).
 *
 * Es el "problema del viajante" en su versión de camino abierto, que no tiene un algoritmo exacto rápido para
 * cualquier cantidad de puntos. Por eso:
 *  - Hasta {@value #LIMITE_EXACTO} paradas: Held-Karp (programación dinámica). Devuelve el orden ÓPTIMO para la
 *    matriz recibida (O(n²·2ⁿ): con 12 paradas son ~600 mil operaciones).
 *  - Más paradas: vecino más cercano + mejora 2-opt. Es una HEURÍSTICA: suele quedar cerca del óptimo pero no lo
 *    garantiza.
 * "Óptimo" es siempre respecto de la matriz (tiempos estimados de OSRM o, si no respondió, distancias en línea
 * recta): no contempla tránsito real, horarios de los comercios ni capacidad del vehículo.
 * La matriz puede ser asimétrica; el 2-opt recalcula el costo completo de cada candidato por eso.
 */
public final class OptimizadorRecorrido {

    public static final int LIMITE_EXACTO = 12;

    private OptimizadorRecorrido() {
    }

    /**
     * @param orden  índices de la matriz (1..n) en el orden de visita
     * @param costo  costo total del recorrido abierto desde el índice 0
     * @param exacto true si es el óptimo (Held-Karp); false si es heurístico
     */
    public record Resultado(List<Integer> orden, double costo, boolean exacto) {
    }

    public static Resultado optimizar(double[][] costos) {
        int destinos = costos.length - 1;
        if (destinos <= 0) {
            return new Resultado(List.of(), 0, true);
        }
        if (destinos <= LIMITE_EXACTO) {
            return heldKarp(costos);
        }
        List<Integer> orden = mejorar2Opt(costos, vecinoMasCercano(costos));
        return new Resultado(orden, costo(costos, orden), false);
    }

    /** Costo del recorrido abierto 0 → orden[0] → orden[1] → … */
    public static double costo(double[][] costos, List<Integer> orden) {
        double total = 0;
        int actual = 0;
        for (int siguiente : orden) {
            total += costos[actual][siguiente];
            actual = siguiente;
        }
        return total;
    }

    // ------------------------------------------------------------------ exacto

    private static Resultado heldKarp(double[][] c) {
        int n = c.length - 1;               // destinos 1..n; bit (j-1) representa al destino j
        int completos = 1 << n;
        double[][] dp = new double[completos][n + 1];
        int[][] previo = new int[completos][n + 1];
        for (double[] fila : dp) {
            java.util.Arrays.fill(fila, Double.POSITIVE_INFINITY);
        }
        for (int j = 1; j <= n; j++) {
            dp[1 << (j - 1)][j] = c[0][j];
            previo[1 << (j - 1)][j] = 0;
        }
        for (int mascara = 1; mascara < completos; mascara++) {
            for (int j = 1; j <= n; j++) {
                double actual = dp[mascara][j];
                if ((mascara & (1 << (j - 1))) == 0 || actual == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int k = 1; k <= n; k++) {
                    int bit = 1 << (k - 1);
                    if ((mascara & bit) != 0) {
                        continue;
                    }
                    double candidato = actual + c[j][k];
                    if (candidato < dp[mascara | bit][k]) {
                        dp[mascara | bit][k] = candidato;
                        previo[mascara | bit][k] = j;
                    }
                }
            }
        }
        int todos = completos - 1;
        int ultimo = 1;
        for (int j = 2; j <= n; j++) {
            if (dp[todos][j] < dp[todos][ultimo]) {
                ultimo = j;
            }
        }
        double total = dp[todos][ultimo];
        List<Integer> orden = new ArrayList<>();
        int mascara = todos;
        int j = ultimo;
        while (j != 0) {
            orden.add(0, j);
            int anterior = previo[mascara][j];
            mascara &= ~(1 << (j - 1));
            j = anterior;
        }
        return new Resultado(orden, total, true);
    }

    // ------------------------------------------------------------------ heurística

    static List<Integer> vecinoMasCercano(double[][] c) {
        int n = c.length - 1;
        boolean[] visitado = new boolean[n + 1];
        List<Integer> orden = new ArrayList<>();
        int actual = 0;
        for (int paso = 0; paso < n; paso++) {
            int mejor = -1;
            for (int k = 1; k <= n; k++) {
                if (!visitado[k] && (mejor == -1 || c[actual][k] < c[actual][mejor])) {
                    mejor = k;
                }
            }
            visitado[mejor] = true;
            orden.add(mejor);
            actual = mejor;
        }
        return orden;
    }

    /** 2-opt: invierte tramos mientras eso acorte el recorrido (mejor mejora de cada pasada). */
    static List<Integer> mejorar2Opt(double[][] c, List<Integer> inicial) {
        List<Integer> orden = new ArrayList<>(inicial);
        double mejorCosto = costo(c, orden);
        boolean mejoro = true;
        int pasadas = 0;
        while (mejoro && pasadas++ < 1000) {
            mejoro = false;
            for (int i = 0; i < orden.size() - 1; i++) {
                for (int k = i + 1; k < orden.size(); k++) {
                    List<Integer> candidato = new ArrayList<>(orden);
                    java.util.Collections.reverse(candidato.subList(i, k + 1));
                    double costoCandidato = costo(c, candidato);
                    if (costoCandidato + 1e-9 < mejorCosto) {
                        orden = candidato;
                        mejorCosto = costoCandidato;
                        mejoro = true;
                    }
                }
            }
        }
        return orden;
    }
}
