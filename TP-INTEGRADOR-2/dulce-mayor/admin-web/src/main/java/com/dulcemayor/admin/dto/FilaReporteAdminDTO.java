package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Fila agregada de un reporte devuelta por la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilaReporteAdminDTO {

    private String grupo;
    private long pedidos;
    private long presentaciones;
    private BigDecimal importe;
}
