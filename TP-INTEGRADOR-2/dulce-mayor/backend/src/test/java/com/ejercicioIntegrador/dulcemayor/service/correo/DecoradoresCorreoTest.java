package com.ejercicioIntegrador.dulcemayor.service.correo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Patrón Decorator del correo: cada decorador concreto se prueba solo, envolviendo un componente falso, y después
 * la cadena completa armada como en ConfiguracionCorreo. No se envía ningún correo real.
 */
class DecoradoresCorreoTest {

    private static final MensajeCorreo MENSAJE =
            new MensajeCorreo("ana@kiosco.com", "Pedido N.º 1002 · Dulce Mayor", "<p>Hola</p>", null);

    /** Componente falso: registra cada envío y falla las primeras veces que se le indique. */
    private static final class ComponenteFalso implements ComponenteCorreo {
        private final List<MensajeCorreo> recibidos = new ArrayList<>();
        private int fallasPendientes;
        private boolean fallaReintentable = true;

        @Override
        public void enviar(MensajeCorreo mensaje) {
            recibidos.add(mensaje);
            if (fallasPendientes > 0) {
                fallasPendientes--;
                throw new ErrorEnvioCorreoException("SMTP no responde", fallaReintentable, null);
            }
        }
    }

    @Test
    void elDecoradorHabilitadoNoDelegaSiElCorreoEstaApagado() {
        ComponenteFalso smtp = new ComponenteFalso();

        new DecoradorConcretoCorreoHabilitado(smtp, false).enviar(MENSAJE);

        assertTrue(smtp.recibidos.isEmpty());
    }

    @Test
    void elDecoradorHabilitadoDelegaElMismoMensajeSiEstaEncendido() {
        ComponenteFalso smtp = new ComponenteFalso();

        new DecoradorConcretoCorreoHabilitado(smtp, true).enviar(MENSAJE);

        assertEquals(1, smtp.recibidos.size());
        assertSame(MENSAJE, smtp.recibidos.get(0));
    }

    @Test
    void elDecoradorToleranteAbsorbeLaFalla() {
        ComponenteFalso smtp = new ComponenteFalso();
        smtp.fallasPendientes = 1;

        new DecoradorConcretoCorreoTolerante(smtp).enviar(MENSAJE);   // no lanza

        assertEquals(1, smtp.recibidos.size());
    }

    @Test
    void losReintentosRepitenLasFallasTransitoriasHastaQueSale() {
        ComponenteFalso smtp = new ComponenteFalso();
        smtp.fallasPendientes = 2;

        new DecoradorConcretoCorreoReintentos(smtp, 3, 0).enviar(MENSAJE);

        assertEquals(3, smtp.recibidos.size());
    }

    @Test
    void losReintentosSeRindenAlAgotarLosIntentosYPropaganLaFalla() {
        ComponenteFalso smtp = new ComponenteFalso();
        smtp.fallasPendientes = 10;

        assertThrows(ErrorEnvioCorreoException.class,
                () -> new DecoradorConcretoCorreoReintentos(smtp, 3, 0).enviar(MENSAJE));
        assertEquals(3, smtp.recibidos.size());
    }

    @Test
    void unaFallaQueNoSeArreglaReintentandoNoSeReintenta() {
        ComponenteFalso smtp = new ComponenteFalso();
        smtp.fallasPendientes = 1;
        smtp.fallaReintentable = false;   // por ejemplo, el destinatario no tiene correo

        assertThrows(ErrorEnvioCorreoException.class,
                () -> new DecoradorConcretoCorreoReintentos(smtp, 3, 0).enviar(MENSAJE));
        assertEquals(1, smtp.recibidos.size());
    }

    @Test
    void laCadenaCompletaReintentaYNuncaInterrumpeAQuienEnvia() {
        ComponenteFalso smtp = new ComponenteFalso();
        smtp.fallasPendientes = 10;
        // Igual que ConfiguracionCorreo: Habilitado → Tolerante → Reintentos → SMTP
        ComponenteCorreo cadena = new DecoradorConcretoCorreoHabilitado(
                new DecoradorConcretoCorreoTolerante(
                        new DecoradorConcretoCorreoReintentos(smtp, 3, 0)), true);

        cadena.enviar(MENSAJE);   // no lanza aunque el servidor nunca responda

        assertEquals(3, smtp.recibidos.size());
    }

    @Test
    void conLaCadenaApagadaNoSeIntentaNada() {
        ComponenteFalso smtp = new ComponenteFalso();
        ComponenteCorreo cadena = new DecoradorConcretoCorreoHabilitado(
                new DecoradorConcretoCorreoTolerante(
                        new DecoradorConcretoCorreoReintentos(smtp, 3, 0)), false);

        cadena.enviar(MENSAJE);

        assertTrue(smtp.recibidos.isEmpty());
    }

    @Test
    void losDecoradoresValidanSuConfiguracion() {
        ComponenteFalso smtp = new ComponenteFalso();
        assertThrows(IllegalArgumentException.class, () -> new DecoradorConcretoCorreoTolerante(null));
        assertThrows(IllegalArgumentException.class, () -> new DecoradorConcretoCorreoReintentos(smtp, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DecoradorConcretoCorreoReintentos(smtp, 3, -1));
    }
}
