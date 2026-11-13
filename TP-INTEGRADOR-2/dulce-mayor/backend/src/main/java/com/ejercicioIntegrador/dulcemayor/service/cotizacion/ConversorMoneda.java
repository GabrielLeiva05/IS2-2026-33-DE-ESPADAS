package com.ejercicioIntegrador.dulcemayor.service.cotizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Conversión ARS → USD con BigDecimal (sin errores de redondeo de double). */
public final class ConversorMoneda {

    private ConversorMoneda() {
    }

    /** Pesos divididos por la cotización de venta, redondeado a centavos de dólar. */
    public static BigDecimal aDolares(BigDecimal pesos, BigDecimal cotizacionVenta) {
        if (pesos == null || cotizacionVenta == null) {
            return null;
        }
        if (cotizacionVenta.signum() <= 0) {
            throw new IllegalArgumentException("La cotización debe ser mayor a cero");
        }
        return pesos.divide(cotizacionVenta, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal aDolares(double pesos, BigDecimal cotizacionVenta) {
        return aDolares(BigDecimal.valueOf(pesos), cotizacionVenta);
    }
}
