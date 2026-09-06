package com.is2.tinder.dtos;

import com.is2.tinder.entities.Zona;

public class ZonaDTO {
    private String id;
    private String nombre;
    private String descripcion;

    public static ZonaDTO fromEntity(Zona zona) {
        ZonaDTO dto = new ZonaDTO();
        dto.id = zona.getId();
        dto.nombre = zona.getNombre();
        dto.descripcion = zona.getDescripcion();
        return dto;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}