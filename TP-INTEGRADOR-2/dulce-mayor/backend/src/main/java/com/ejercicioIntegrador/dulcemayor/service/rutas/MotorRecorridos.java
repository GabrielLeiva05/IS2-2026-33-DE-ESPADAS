package com.ejercicioIntegrador.dulcemayor.service.rutas;

import com.ejercicioIntegrador.dulcemayor.dto.TrayectoDTO;

import java.util.List;

/**
 * Motor de recorridos por calles que usa el servicio de rutas. Cada punto es {latitud, longitud}.
 *
 * La implementación actual es OSRM (ServicioMapas): la comparación con GraphHopper y la decisión están en el
 * README (secciones 13 a 15). Si el equipo cambiara de motor, alcanza con otra implementación de esta interfaz.
 */
public interface MotorRecorridos {

    /**
     * Matriz de costos entre todos los puntos (fila = origen, columna = destino). Si el motor no responde,
     * se devuelve la matriz en línea recta y {@link MatrizRecorrido#porCalles()} lo indica.
     */
    MatrizRecorrido matriz(List<double[]> puntos);

    /** Trayecto dibujable que pasa por los puntos EN EL ORDEN DADO. */
    TrayectoDTO trazar(List<double[]> puntos);
}
