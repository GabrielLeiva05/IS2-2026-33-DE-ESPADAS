package com.ejercicioIntegrador.dulcemayor.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodigosCanjeOAuth2Test {

    @Test
    void cadaCodigoEsDistintoYSeUsaUnaSolaVez() {
        CodigosCanjeOAuth2 codigos = new CodigosCanjeOAuth2();
        String primero = codigos.emitir("a@mail.com");
        String segundo = codigos.emitir("a@mail.com");

        assertNotEquals(primero, segundo);
        assertTrue(primero.length() >= 43);
        assertEquals(Optional.of("a@mail.com"), codigos.canjear(primero));
        assertEquals(Optional.empty(), codigos.canjear(primero));
    }

    @Test
    void elCodigoVenceALos60Segundos() {
        MutableClock reloj = new MutableClock(Instant.parse("2026-11-01T10:00:00Z"));
        CodigosCanjeOAuth2 codigos = new CodigosCanjeOAuth2(reloj);
        String codigo = codigos.emitir("a@mail.com");

        reloj.avanzar(Duration.ofSeconds(61));

        assertEquals(Optional.empty(), codigos.canjear(codigo));
    }

    /** Reloj controlable para la prueba de vencimiento. */
    private static final class MutableClock extends Clock {
        private Instant ahora;

        MutableClock(Instant inicio) {
            this.ahora = inicio;
        }

        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }
}
