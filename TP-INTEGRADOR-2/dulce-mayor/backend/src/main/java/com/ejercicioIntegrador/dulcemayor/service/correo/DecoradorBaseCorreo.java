package com.ejercicioIntegrador.dulcemayor.service.correo;

/**
 * PATRÓN DECORATOR — DECORADOR BASE.
 *
 * Guarda una referencia al componente envuelto, declarada con el tipo de la interfaz (ComponenteCorreo) para que
 * pueda ser el componente concreto u otro decorador, y delega en él. Los decoradores concretos redefinen
 * enviar() y agregan su comportamiento antes o después de llamar a super.enviar().
 */
public abstract class DecoradorBaseCorreo implements ComponenteCorreo {

    private final ComponenteCorreo envuelto;

    protected DecoradorBaseCorreo(ComponenteCorreo envuelto) {
        if (envuelto == null) {
            throw new IllegalArgumentException("El decorador necesita un componente que envolver");
        }
        this.envuelto = envuelto;
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        envuelto.enviar(mensaje);
    }
}
