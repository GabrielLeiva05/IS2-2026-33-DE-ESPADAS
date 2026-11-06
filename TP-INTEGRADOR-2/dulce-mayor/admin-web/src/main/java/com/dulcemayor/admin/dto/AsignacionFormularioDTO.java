package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Formulario de asignación de pedidos pendientes de entrega a un distribuidor. */
@Getter
@Setter
@NoArgsConstructor
public class AsignacionFormularioDTO {

    private String idDistribuidor;
    private List<String> idsPedido = new ArrayList<>();
}
