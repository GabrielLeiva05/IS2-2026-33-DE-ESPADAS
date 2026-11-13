package com.ejercicioIntegrador.dulcemayor.exceptions;

/** Se superó un límite de uso (por ejemplo, mensajes por minuto del asistente). Se responde 429. */
public class LimiteExcedidoException extends MiException {

    public LimiteExcedidoException(String mensaje) {
        super(mensaje);
    }
}
