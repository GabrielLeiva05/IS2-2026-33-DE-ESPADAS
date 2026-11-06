package com.ejercicioIntegrador.dulcemayor.service;

/**
 * Distancia en línea recta entre dos coordenadas (fórmula de Haversine, radio terrestre medio 6371 km).
 * Es la medida que usa el ordenamiento por cercanía de las rutas: no considera calles ni sentidos de circulación.
 */
public final class CalculoDistancia {

    private static final double RADIO_TIERRA_KM = 6371.0;

    private CalculoDistancia() {
    }

    public static double kilometros(double latitud1, double longitud1, double latitud2, double longitud2) {
        double dLat = Math.toRadians(latitud2 - latitud1);
        double dLon = Math.toRadians(longitud2 - longitud1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud1)) * Math.cos(Math.toRadians(latitud2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
    }
}
