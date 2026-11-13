package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

import com.ejercicioIntegrador.dulcemayor.dto.CotizacionDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cotización del dólar: caché, tolerancia a fallas de la fuente y conversión con BigDecimal. */
class ServicioCotizacionTest {

    /** Reloj que la prueba hace avanzar. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-11-13T12:00:00Z");

        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    /** Fuente falsa: cuenta las consultas y puede fallar. */
    private static final class FuenteFalsa implements ProveedorCotizacion {
        int consultas;
        boolean falla;
        BigDecimal venta = new BigDecimal("1450.00");

        @Override
        public Cotizacion consultar(String tipo) {
            consultas++;
            if (falla) {
                throw new IllegalStateException("sin conexión");
            }
            return new Cotizacion(tipo, "Oficial", new BigDecimal("1400.00"), venta,
                    OffsetDateTime.parse("2026-11-13T11:55:00Z"));
        }
    }

    @Test
    void consultaLaFuenteUnaVezYUsaLaCacheMientrasEsteVigente() {
        FuenteFalsa fuente = new FuenteFalsa();
        RelojManual reloj = new RelojManual();
        ServicioCotizacion servicio = new ServicioCotizacion(fuente, "oficial", 10, reloj);

        CotizacionDTO primera = servicio.actual().orElseThrow();
        servicio.actual();
        reloj.avanzar(Duration.ofMinutes(9));
        servicio.actual();

        assertEquals(1, fuente.consultas);
        assertEquals(new BigDecimal("1450.00"), primera.venta());
        assertEquals("oficial", primera.tipo());
        assertFalse(primera.desactualizada());

        reloj.avanzar(Duration.ofMinutes(2));     // pasaron 11 minutos: se vuelve a consultar
        fuente.venta = new BigDecimal("1460.00");
        assertEquals(new BigDecimal("1460.00"), servicio.actual().orElseThrow().venta());
        assertEquals(2, fuente.consultas);
    }

    @Test
    void siLaFuenteFallaUsaLaUltimaMarcadaComoDesactualizadaYNoReintentaEnseguida() {
        FuenteFalsa fuente = new FuenteFalsa();
        RelojManual reloj = new RelojManual();
        ServicioCotizacion servicio = new ServicioCotizacion(fuente, "oficial", 10, reloj);
        servicio.actual();

        fuente.falla = true;
        reloj.avanzar(Duration.ofMinutes(11));
        CotizacionDTO vieja = servicio.actual().orElseThrow();
        servicio.actual();                        // dentro del minuto de espera: no vuelve a consultar

        assertTrue(vieja.desactualizada());
        assertEquals(new BigDecimal("1450.00"), vieja.venta());
        assertEquals(2, fuente.consultas);

        fuente.falla = false;
        reloj.avanzar(Duration.ofMinutes(2));
        assertFalse(servicio.actual().orElseThrow().desactualizada());
        assertEquals(3, fuente.consultas);
    }

    @Test
    void sinNingunaCotizacionDevuelveVacioSinRomper() {
        FuenteFalsa fuente = new FuenteFalsa();
        fuente.falla = true;
        ServicioCotizacion servicio = new ServicioCotizacion(fuente, "bolsa", 10, new RelojManual());

        assertTrue(servicio.actual().isEmpty());
        assertTrue(servicio.ventaVigente().isEmpty());
    }

    @Test
    void elTipoDeCotizacionEsConfigurableYSeValida() {
        assertThrows(IllegalArgumentException.class, () -> new ServicioCotizacion(new FuenteFalsa(), "euro", 10, new RelojManual()));
        assertEquals("bolsa", new ServicioCotizacion(new FuenteFalsa(), " BOLSA ", 10, new RelojManual()).actual().orElseThrow().tipo());
    }

    @Test
    void conversionConBigDecimalRedondeadaACentavos() {
        assertEquals(new BigDecimal("12.41"), ConversorMoneda.aDolares(new BigDecimal("18000"), new BigDecimal("1450")));   // 12,4137…
        assertEquals(new BigDecimal("0.10"), ConversorMoneda.aDolares(0.1 + 0.2 - 0.15, new BigDecimal("1.5")));
        assertNull(ConversorMoneda.aDolares(new BigDecimal("100"), null));
        assertThrows(IllegalArgumentException.class, () -> ConversorMoneda.aDolares(BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void laFechaDeDolarApiSeInterpretaYUnaFechaInvalidaNoRompe() {
        assertEquals(OffsetDateTime.parse("2026-11-13T15:00:00.000Z"), ProveedorCotizacionDolarApi.fecha("2026-11-13T15:00:00.000Z"));
        assertNull(ProveedorCotizacionDolarApi.fecha("ayer"));
        assertNull(ProveedorCotizacionDolarApi.fecha(null));
    }
}
