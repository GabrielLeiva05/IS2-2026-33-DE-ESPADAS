package com.tienda.carrito.controller.view;

/** Casos de uso de pantalla. Se le informan a los hooks preUseCase()/postUseCase() de cada controlador. */
public enum BaseUseCaseController {
    LISTAR,
    ALTA,
    CONSULTAR,
    MODIFICAR,
    BAJA,
    ACTUALIZAR
}
