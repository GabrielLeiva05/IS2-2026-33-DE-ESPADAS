package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(String id, String idPuntoVenta, String nombreComercial, String idPreventista,
                                EstadoPedido estado, List<PedidoLineaResponseDTO> lineas, BigDecimal subtotal,
                                BigDecimal descuento, String promocionCodigo, BigDecimal total,
                                LocalDateTime fechaCreacion, LocalDateTime fechaConfirmacion,
                                LocalDateTime fechaCancelacion, LocalDateTime fechaCompletado)
        implements BaseResponseDTO {
}
