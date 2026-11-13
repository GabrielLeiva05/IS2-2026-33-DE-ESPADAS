package com.ejercicioIntegrador.dulcemayor.service.correo;

/**
 * Falla al enviar un correo. {@link #isReintentable()} indica si vale la pena volver a intentar
 * (por ejemplo, el servidor SMTP no respondió) o no (falta configuración o destinatario).
 */
public class ErrorEnvioCorreoException extends RuntimeException {

    private final boolean reintentable;

    public ErrorEnvioCorreoException(String mensaje, boolean reintentable, Throwable causa) {
        super(mensaje, causa);
        this.reintentable = reintentable;
    }

    public boolean isReintentable() {
        return reintentable;
    }
}
