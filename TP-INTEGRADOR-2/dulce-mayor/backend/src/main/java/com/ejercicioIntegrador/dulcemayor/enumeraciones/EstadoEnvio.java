package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/** Resultado del intento de envío de un correo. */
public enum EstadoEnvio {
    /** Se entregó al servidor SMTP configurado. */
    ENVIADO,
    /** No hay SMTP configurado: el envío quedó registrado sin salir del servidor. */
    SIMULADO,
    FALLIDO
}
