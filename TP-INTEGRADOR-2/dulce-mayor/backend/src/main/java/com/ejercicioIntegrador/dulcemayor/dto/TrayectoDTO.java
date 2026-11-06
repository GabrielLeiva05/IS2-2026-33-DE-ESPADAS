package com.ejercicioIntegrador.dulcemayor.dto;

import java.util.List;

/**
 * Recorrido dibujable: lista de [latitud, longitud]. "fuente" indica si sigue las calles (OSRM)
 * o si es una línea recta entre los puntos porque el servicio de ruteo no respondió.
 */
public record TrayectoDTO(List<List<Double>> geometria, double kilometros, double minutos, String fuente) {

    public static TrayectoDTO vacio() {
        return new TrayectoDTO(List.of(), 0, 0, "sin recorrido");
    }
}
