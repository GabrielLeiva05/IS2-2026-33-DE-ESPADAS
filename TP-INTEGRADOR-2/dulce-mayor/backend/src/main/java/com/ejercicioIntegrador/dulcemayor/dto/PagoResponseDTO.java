package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago del pedido. Para Mercado Pago, {@code qrContenido} trae el EMVCo del QR dinámico y
 * {@code qrImagenBase64} la misma imagen lista para mostrar (Checkout API, sin redirección).
 */
public record PagoResponseDTO(String id, String idPedido, MedioPago medio, EstadoPago estado, BigDecimal importe,
                              String referenciaOperacion, String referenciaExterna, String instrucciones,
                              String qrContenido, String qrImagenBase64, LocalDateTime fechaCreacion,
                              LocalDateTime fechaVencimiento, LocalDateTime fechaAcreditacion,
                              String observacion) implements BaseResponseDTO {
}
