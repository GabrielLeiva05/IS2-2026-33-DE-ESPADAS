package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Punto de una serie (pedidos por preventista por día o mes). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PuntoSerieAdminDTO {

    private String periodo;
    private String serie;
    private long cantidad;
    private BigDecimal importe;
}
