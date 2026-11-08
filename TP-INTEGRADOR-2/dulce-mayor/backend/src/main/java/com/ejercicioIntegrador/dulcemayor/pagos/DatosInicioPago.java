package com.ejercicioIntegrador.dulcemayor.pagos;

/**
 * Datos que la estrategia devuelve al iniciar un cobro. Los medios manuales solo aportan
 * instrucciones; Mercado Pago agrega el contenido del QR dinámico que se envía por correo.
 * No hay enlace de redirección: el cobro se completa dentro del mismo flujo (Checkout API).
 */
public record DatosInicioPago(String instrucciones, String referenciaExterna, String qrContenido) {

    public static DatosInicioPago manual(String instrucciones) {
        return new DatosInicioPago(instrucciones, null, null);
    }

    public static DatosInicioPago conQr(String instrucciones, String referenciaExterna, String qrContenido) {
        return new DatosInicioPago(instrucciones, referenciaExterna, qrContenido);
    }
}
