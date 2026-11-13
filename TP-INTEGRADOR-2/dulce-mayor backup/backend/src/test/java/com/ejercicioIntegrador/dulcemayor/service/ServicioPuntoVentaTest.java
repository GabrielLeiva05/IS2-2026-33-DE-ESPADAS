package com.ejercicioIntegrador.dulcemayor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validación del CUIT de los puntos de venta (HU-17: CUIT único y válido). */
class ServicioPuntoVentaTest {

    @Test
    void aceptaCuitConDigitoVerificadorCorrecto() {
        assertTrue(ServicioPuntoVenta.cuitValido("20123456786"));
        assertTrue(ServicioPuntoVenta.cuitValido("30712345671"));
    }

    @Test
    void rechazaCuitConDigitoIncorrectoOFormatoInvalido() {
        assertFalse(ServicioPuntoVenta.cuitValido("20123456780"));
        assertFalse(ServicioPuntoVenta.cuitValido("2012345678"));
        assertFalse(ServicioPuntoVenta.cuitValido("20-12345678-6"));
        assertFalse(ServicioPuntoVenta.cuitValido(null));
    }
}
