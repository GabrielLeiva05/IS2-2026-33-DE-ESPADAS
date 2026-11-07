package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Cotización informativa del dólar publicada por la API. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CotizacionDTO {

    private BigDecimal valor;
    private String fechaActualizacion;
    private boolean actualizada;
    private String mensaje;

    public String getValorLegible() {
        return valor == null ? "sin datos" : "$ " + String.format(new java.util.Locale("es", "AR"), "%,.2f", valor);
    }
}
