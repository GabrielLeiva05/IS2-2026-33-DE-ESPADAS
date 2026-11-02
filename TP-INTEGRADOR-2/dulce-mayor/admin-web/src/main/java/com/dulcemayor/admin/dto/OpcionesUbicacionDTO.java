package com.dulcemayor.admin.dto;

import java.util.List;

/** Opciones de los cuatro selects de ubicación de un formulario. */
public record OpcionesUbicacionDTO(List<OpcionDTO> paises, List<OpcionDTO> provincias,
                                   List<OpcionDTO> departamentos, List<OpcionDTO> localidades) {

    public List<OpcionDTO> getPaises() {
        return paises;
    }

    public List<OpcionDTO> getProvincias() {
        return provincias;
    }

    public List<OpcionDTO> getDepartamentos() {
        return departamentos;
    }

    public List<OpcionDTO> getLocalidades() {
        return localidades;
    }
}
