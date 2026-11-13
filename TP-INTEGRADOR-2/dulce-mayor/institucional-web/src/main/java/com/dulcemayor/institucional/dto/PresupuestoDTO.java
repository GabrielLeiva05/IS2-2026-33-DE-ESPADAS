package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Presupuesto del asistente (orientativo, sin IVA; no reserva stock). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresupuestoDTO {
    private List<RenglonPresupuestoDTO> renglones = List.of();
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private BigDecimal totalUsd;
    private BigDecimal cotizacion;
    private List<String> avisos = List.of();
}
