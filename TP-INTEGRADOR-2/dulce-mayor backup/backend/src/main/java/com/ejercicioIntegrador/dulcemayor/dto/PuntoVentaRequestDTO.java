package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.CondicionIva;

/**
 * Alta y modificación de un punto de venta (HU-17). La dirección es la comercial y debe traer latitud y longitud,
 * obtenidas buscando la dirección (geocodificación) o tocando el mapa.
 */
public record PuntoVentaRequestDTO(String nombreFantasia, String razonSocial, String cuit, CondicionIva condicionIva,
                                   String idCliente, DireccionRequestDTO direccion) {
}
