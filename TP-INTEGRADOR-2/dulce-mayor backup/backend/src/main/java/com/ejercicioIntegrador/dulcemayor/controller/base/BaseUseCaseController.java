package com.ejercicioIntegrador.dulcemayor.controller.base;

/**
 * Casos de uso de la capa de controladores (mismo enfoque que el ejemplo patronTemplate).
 * En una API REST alta y modificacion son endpoints distintos (POST y PUT), por eso no
 * existe el caso ACTUALIZAR del ejemplo, que unificaba ambos en un formulario.
 */
public enum BaseUseCaseController {
    ALTA,
    MODIFICACION,
    CONSULTAR,
    BAJA,
    LISTAR
}
