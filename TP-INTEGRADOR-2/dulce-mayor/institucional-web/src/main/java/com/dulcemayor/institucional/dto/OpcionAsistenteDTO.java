package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Opción que ofrece el asistente cuando el pedido es ambiguo. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpcionAsistenteDTO {
    private String idProducto;
    private String nombre;
    private String presentacion;
    private BigDecimal precio;
}
