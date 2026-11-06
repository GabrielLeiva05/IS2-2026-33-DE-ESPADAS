package com.ejercicioIntegrador.dulcemayor.pagos;

/**
 * Datos que la estrategia devuelve al iniciar un cobro. Los medios manuales no tienen enlace ni
 * referencia externa: solo instrucciones. No se finge que todos los medios ofrezcan lo mismo.
 */
public record DatosInicioPago(String instrucciones, String enlacePago, String referenciaExterna) {

    public static DatosInicioPago manual(String instrucciones) {
        return new DatosInicioPago(instrucciones, null, null);
    }
}
