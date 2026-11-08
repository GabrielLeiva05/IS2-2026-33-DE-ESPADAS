package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factura de cliente o de proveedor (tipo = CLIENTE | PROVEEDOR). Importes: subtotal sin IVA, descuento, IVA
 * discriminado (alicuotaIva, por ejemplo 0.21) y total con IVA. Solo lleva los datos necesarios para mostrarla:
 * nada de usuarios, claves ni datos de pago.
 */
public record FacturaResponseDTO(String id, String tipo, long numeroFactura, LocalDateTime fechaFactura,
                                 EstadoFactura estado, double subtotal, double descuento, double iva,
                                 double alicuotaIva, double total,
                                 String idPedido, Long numeroPedido, String idCliente, String nombreCliente,
                                 String nombreFantasia,
                                 String idProveedor, String razonSocialProveedor, String cuitProveedor,
                                 List<DetalleFacturaResponseDTO> detalles, boolean eliminado) implements BaseResponseDTO {
}
