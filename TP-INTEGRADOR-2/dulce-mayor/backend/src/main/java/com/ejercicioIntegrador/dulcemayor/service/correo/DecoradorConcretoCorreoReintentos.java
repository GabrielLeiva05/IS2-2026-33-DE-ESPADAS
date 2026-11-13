package com.ejercicioIntegrador.dulcemayor.service.correo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PATRÓN DECORATOR — DECORADOR CONCRETO: reintenta el envío cuando la falla puede ser transitoria
 * (el servidor SMTP no respondió). Las fallas que no se arreglan reintentando (falta configuración o
 * destinatario, dirección inválida) se propagan enseguida.
 *
 * Entre intentos espera una pausa corta y creciente (pausa, 2 × pausa, …). Si se agotan los intentos, propaga
 * la última falla: decidir qué hacer con ella le corresponde a la capa de afuera (el decorador tolerante).
 */
public class DecoradorConcretoCorreoReintentos extends DecoradorBaseCorreo {

    private static final Logger log = LoggerFactory.getLogger(DecoradorConcretoCorreoReintentos.class);

    private final int intentos;
    private final long pausaMilisegundos;

    public DecoradorConcretoCorreoReintentos(ComponenteCorreo envuelto, int intentos, long pausaMilisegundos) {
        super(envuelto);
        if (intentos < 1) {
            throw new IllegalArgumentException("Tiene que haber al menos un intento");
        }
        if (pausaMilisegundos < 0) {
            throw new IllegalArgumentException("La pausa entre intentos no puede ser negativa");
        }
        this.intentos = intentos;
        this.pausaMilisegundos = pausaMilisegundos;
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        for (int intento = 1; ; intento++) {
            try {
                super.enviar(mensaje);
                return;
            } catch (ErrorEnvioCorreoException e) {
                if (!e.isReintentable() || intento >= intentos) {
                    throw e;
                }
                log.info("Falló el envío de «{}» (intento {} de {}), se reintenta: {}", mensaje.asunto(), intento,
                        intentos, e.getMessage());
                esperar(pausaMilisegundos * intento);
            }
        }
    }

    private static void esperar(long milisegundos) {
        if (milisegundos == 0) {
            return;
        }
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorEnvioCorreoException("Se interrumpió la espera entre reintentos", false, e);
        }
    }
}
