package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.PuntoRuta;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ordenamiento por cercanía (vecino más cercano) de los puntos pendientes de una ruta. */
class ServicioRutaTest {

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
    void ordenaDesdeLaUbicacionVisitandoSiempreElMasCercano() {
        PuntoRuta lejos = parada("lejos", -32.95, -68.84);
        PuntoRuta medio = parada("medio", -32.91, -68.84);
        PuntoRuta cerca = parada("cerca", -32.89, -68.84);

        List<PuntoRuta> orden = ServicioRuta.ordenarPorCercania(-32.88, -68.84, List.of(lejos, cerca, medio));

        assertEquals(List.of("cerca", "medio", "lejos"), orden.stream().map(p -> p.getPuntoVenta().getId()).toList());
    }

    @Test
    void haversineDaDistanciasRazonables() {
        // Un grado de latitud son unos 111 km
        double km = CalculoDistancia.kilometros(-32.0, -68.0, -33.0, -68.0);
        assertTrue(km > 110 && km < 112);
    }
}
