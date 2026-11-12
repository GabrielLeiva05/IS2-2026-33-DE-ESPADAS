package com.ejercicioIntegrador.dulcemayor.exportacion;

/**
 * Falla técnica al generar un archivo (por ejemplo, un error de la biblioteca de PDF o de Excel).
 * No es un error del usuario: ManejadorErrores la responde como error de sistema (500) y la registra en el log.
 */
public class ErrorExportacionException extends RuntimeException {

    public ErrorExportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
