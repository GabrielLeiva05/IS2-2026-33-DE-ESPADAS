package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Filtros de los reportes tal como llegan del formulario (texto): la API los valida y los interpreta. */
@Getter
@Setter
@NoArgsConstructor
public class FiltroReporteFormularioDTO {

    private String desde;
    private String hasta;
    private String idDepartamento;
    private String idPreventista;
    private String estado;
}
