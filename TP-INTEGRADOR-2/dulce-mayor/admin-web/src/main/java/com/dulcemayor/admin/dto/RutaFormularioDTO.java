package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Formulario "Armar ruta de visitas" (HU-18): preventista, fecha y puntos de venta en el orden elegido. */
@Getter
@Setter
@NoArgsConstructor
public class RutaFormularioDTO {

    private String fecha;
    private String idPreventista;
    private List<String> idsPuntoVenta = new ArrayList<>();
}
