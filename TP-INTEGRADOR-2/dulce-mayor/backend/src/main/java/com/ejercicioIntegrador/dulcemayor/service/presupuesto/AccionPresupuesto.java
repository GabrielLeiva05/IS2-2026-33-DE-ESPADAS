package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

/** Lo que el cliente pidió en su mensaje, según la interpretación de la IA. */
public enum AccionPresupuesto {
    /** Sumar productos (la cantidad es adicional a la que ya tenía). */
    AGREGAR,
    /** Sacar un producto, o restar la cantidad indicada. */
    QUITAR,
    /** Dejar un producto con la cantidad indicada. */
    CAMBIAR_CANTIDAD,
    /** Armar una propuesta que entre en un monto. */
    RECOMENDAR,
    /** Ver el presupuesto actual. */
    CONSULTAR,
    /** Empezar de cero. */
    VACIAR,
    /** Saludo, pregunta general o algo que no es un presupuesto. */
    OTRA
}
