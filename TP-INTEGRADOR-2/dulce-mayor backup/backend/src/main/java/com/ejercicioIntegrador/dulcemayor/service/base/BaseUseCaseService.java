package com.ejercicioIntegrador.dulcemayor.service.base;

/**
 * Casos de uso de la capa de servicio (mismo enfoque que el ejemplo patronTemplate).
 * Se pasan al paso validar() para que cada servicio concreto sepa en que contexto valida.
 */
public enum BaseUseCaseService {
    ALTA,
    MODIFICACION,
    BAJA,
    CONSULTAR,
    LISTAR
}
