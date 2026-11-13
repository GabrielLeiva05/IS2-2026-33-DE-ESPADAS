package com.ejercicioIntegrador.dulcemayor.enumeraciones;

/** Tipos de promoción comercial (HU-22). */
public enum TipoPromocion {
    /** Porcentaje de descuento sobre productos o subcategorías determinados. */
    PORCENTAJE_PRODUCTO,
    /** Porcentaje según la cantidad comprada de cada producto (tramos configurables). */
    VOLUMEN,
    /** Código que el preventista ingresa al cargar el pedido. */
    CODIGO
}
