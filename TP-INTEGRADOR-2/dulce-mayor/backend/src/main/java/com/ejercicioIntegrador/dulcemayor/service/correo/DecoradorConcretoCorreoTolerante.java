package com.ejercicioIntegrador.dulcemayor.service.correo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PATRÓN DECORATOR — DECORADOR CONCRETO: un correo que no se pudo enviar no interrumpe la operación que lo
 * disparó (confirmar un pedido, acreditar un pago, registrar una entrega).
 *
 * Delega y, si el envío falla, registra el problema en el log en lugar de propagar la excepción.
 */
public class DecoradorConcretoCorreoTolerante extends DecoradorBaseCorreo {

    private static final Logger log = LoggerFactory.getLogger(DecoradorConcretoCorreoTolerante.class);

    public DecoradorConcretoCorreoTolerante(ComponenteCorreo envuelto) {
        super(envuelto);
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        try {
            super.enviar(mensaje);
        } catch (RuntimeException e) {
            log.warn("No se pudo enviar «{}»: {}", mensaje.asunto(), e.getMessage());
        }
    }
}
