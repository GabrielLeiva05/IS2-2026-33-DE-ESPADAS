package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.CondicionIva;

/** Punto de venta con su dirección comercial (incluye coordenadas) y su cliente responsable. */
public record PuntoVentaResponseDTO(String id, String nombreFantasia, String razonSocial, String cuit,
                                    CondicionIva condicionIva, String idCliente, String nombreCliente,
                                    String correoCliente, DireccionResponseDTO direccion,
                                    boolean eliminado) implements BaseResponseDTO {
}
