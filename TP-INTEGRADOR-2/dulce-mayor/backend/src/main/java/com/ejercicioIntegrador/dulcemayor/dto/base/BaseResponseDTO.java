package com.ejercicioIntegrador.dulcemayor.dto.base;

/**
 * Contrato minimo de todo DTO de salida que represente una entidad.
 * BaseController lo usa para armar el encabezado Location al dar de alta un recurso.
 */
public interface BaseResponseDTO {

    String id();
}
