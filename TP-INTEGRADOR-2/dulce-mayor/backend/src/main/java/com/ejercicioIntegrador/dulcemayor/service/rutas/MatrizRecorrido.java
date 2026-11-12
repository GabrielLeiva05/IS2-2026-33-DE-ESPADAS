package com.ejercicioIntegrador.dulcemayor.service.rutas;

import com.ejercicioIntegrador.dulcemayor.service.CalculoDistancia;

import java.util.List;

/**
 * Costos de ir de cada punto a cada otro.
 *
 * @param costos    costos[i][j] = costo de ir de i a j (puede ser asimétrica: calles de una mano)
 * @param porCalles true si salen del motor de recorridos (tiempo de manejo); false si son distancias en línea recta
 * @param criterio  texto para mostrar qué se minimizó
 */
public record MatrizRecorrido(double[][] costos, boolean porCalles, String criterio) {

    /** Matriz en línea recta (Haversine, km). Se usa solo si el motor de recorridos no respondió. */
    public static MatrizRecorrido lineaRecta(List<double[]> puntos) {
        int n = puntos.size();
        double[][] costos = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costos[i][j] = i == j ? 0 : CalculoDistancia.kilometros(puntos.get(i)[0], puntos.get(i)[1],
                        puntos.get(j)[0], puntos.get(j)[1]);
            }
        }
        return new MatrizRecorrido(costos, false, "distancia en línea recta (el servicio de recorridos no respondió)");
    }
}
