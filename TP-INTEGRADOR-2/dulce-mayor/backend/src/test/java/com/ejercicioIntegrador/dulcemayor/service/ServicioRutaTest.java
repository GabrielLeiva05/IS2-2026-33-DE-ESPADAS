package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.TrayectoDTO;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.PuntoRuta;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.rutas.MatrizRecorrido;
import com.ejercicioIntegrador.dulcemayor.service.rutas.MotorRecorridos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Orden de los puntos pendientes de una ruta con el motor de recorridos y el optimizador. */
class ServicioRutaTest {

    /** Motor que "no responde": la matriz sale en línea recta, como cuando OSRM no está disponible. */
    private static final MotorRecorridos SIN_OSRM = new MotorRecorridos() {
        @Override
        public MatrizRecorrido matriz(List<double[]> puntos) {
            return MatrizRecorrido.lineaRecta(puntos);
        }

        @Override
        public TrayectoDTO trazar(List<double[]> puntos) {
            return ServicioMapas.lineaRecta(puntos);
        }
    };

    private static PuntoRuta parada(String id, double latitud, double longitud) {
        Direccion direccion = new Direccion();
        direccion.setLatitud(latitud);
        direccion.setLongitud(longitud);
        PuntoVenta puntoVenta = new PuntoVenta();
        puntoVenta.setId(id);
        puntoVenta.setDireccion(direccion);
        return new PuntoRuta(puntoVenta, 0);
    }

    @Test
    void sinOsrmOrdenaEnLineaRectaYLoInforma() {
        PuntoRuta lejos = parada("lejos", -32.95, -68.84);
        PuntoRuta medio = parada("medio", -32.91, -68.84);
        PuntoRuta cerca = parada("cerca", -32.89, -68.84);
        ServicioRuta servicio = new ServicioRuta(null, null, null, SIN_OSRM);

        ServicioRuta.OrdenCalculado orden = servicio.ordenOptimo(-32.88, -68.84, List.of(lejos, cerca, medio));

        assertEquals(List.of(cerca, medio, lejos), orden.orden());
        assertTrue(orden.criterio().startsWith("Orden óptimo"));
        assertTrue(orden.criterio().contains("línea recta"));
    }

    @Test
    void conTiemposDeManejoSigueLaMatrizDelMotorAunqueNoCoincidaConLaLineaRecta() {
        // "cerca" está más cerca en línea recta, pero por calles (una mano, por ejemplo) conviene ir primero a "otro"
        PuntoRuta cerca = parada("cerca", -32.881, -68.84);
        PuntoRuta otro = parada("otro", -32.89, -68.85);
        MotorRecorridos porCalles = new MotorRecorridos() {
            @Override
            public MatrizRecorrido matriz(List<double[]> puntos) {
                double[][] segundos = {
                        {0, 900, 120},   // desde la ubicación: a "cerca" 15 min, a "otro" 2 min
                        {900, 0, 300},
                        {120, 60, 0}};   // de "otro" a "cerca" 1 min
                return new MatrizRecorrido(segundos, true, "tiempo de manejo estimado por calles (OSRM)");
            }

            @Override
            public TrayectoDTO trazar(List<double[]> puntos) {
                return TrayectoDTO.vacio();
            }
        };

        ServicioRuta.OrdenCalculado orden = new ServicioRuta(null, null, null, porCalles)
                .ordenOptimo(-32.88, -68.84, List.of(cerca, otro));

        assertEquals(List.of(otro, cerca), orden.orden());
        assertTrue(orden.criterio().contains("OSRM"));
    }

    @Test
    void haversineDaDistanciasRazonables() {
        // Un grado de latitud son unos 111 km
        double km = CalculoDistancia.kilometros(-32.0, -68.0, -33.0, -68.0);
        assertTrue(km > 110 && km < 112);
    }
}
