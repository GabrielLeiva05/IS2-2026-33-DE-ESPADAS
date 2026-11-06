package com.ejercicioIntegrador.dulcemayor.dto;

import com.ejercicioIntegrador.dulcemayor.dto.base.BaseResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(String id, String idPedido, MedioPago medio, EstadoPago estado, BigDecimal importe,
                              String referenciaOperacion, String referenciaExterna, String instrucciones,
                              String enlacePago, LocalDateTime fechaCreacion, LocalDateTime fechaVencimiento,
                              LocalDateTime fechaAcreditacion, String observacion) implements BaseResponseDTO {
}
