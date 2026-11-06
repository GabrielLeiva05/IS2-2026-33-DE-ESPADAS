package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.ResultadoVisita;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Parada de la ruta lista para dibujar en el mapa: datos del comercio y del cliente, coordenadas y resultado.
 * En las rutas de ENTREGA incluye los pedidos a entregar en ese comercio.
 */
public record PuntoRutaDTO(int orden, ResultadoVisita resultado, String motivo, LocalDateTime fechaHoraVisita,
                           String idPuntoVenta, String nombreFantasia, String razonSocial, String cuit,
                           String direccion, Double latitud, Double longitud,
                           String idCliente, String nombreCliente, List<PedidoResumenDTO> pedidos) {
}
