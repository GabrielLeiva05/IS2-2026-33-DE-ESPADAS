package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.util.List;

/** Vista previa de un pedido con sus descuentos, calculada en el backend antes de cargarlo. No guarda nada. */
public record SimulacionPedidoDTO(List<RenglonSimuladoDTO> renglones, List<DescuentoPedidoDTO> descuentos,
                                  BigDecimal subtotal, BigDecimal descuento, BigDecimal total, String codigoAplicado) {
}
