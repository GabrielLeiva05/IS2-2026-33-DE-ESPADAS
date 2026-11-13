package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tramo de una promoción por volumen. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromocionTramoDTO {

    private Integer cantidadDesde;
    private Integer cantidadHasta;
    private java.math.BigDecimal porcentaje;
}
