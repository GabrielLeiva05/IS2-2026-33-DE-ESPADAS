package com.ejercicioIntegrador.dulcemayor.service.correo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PATRÓN DECORATOR — DECORADOR CONCRETO: respeta el interruptor app.correo.habilitado (CORREO_HABILITADO).
 *
 * Si el correo está deshabilitado (por ejemplo, en desarrollo o en las pruebas) no llama al componente
 * envuelto: solo deja constancia en el log. Si está habilitado, delega.
 */
public class DecoradorConcretoCorreoHabilitado extends DecoradorBaseCorreo {

    private static final Logger log = LoggerFactory.getLogger(DecoradorConcretoCorreoHabilitado.class);

    private final boolean habilitado;

    public DecoradorConcretoCorreoHabilitado(ComponenteCorreo envuelto, boolean habilitado) {
        super(envuelto);
        this.habilitado = habilitado;
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        if (!habilitado) {
            log.info("Correo deshabilitado: no se envía «{}» (activar CORREO_HABILITADO y MAIL_*)", mensaje.asunto());
            return;
        }
        super.enviar(mensaje);
    }
}
