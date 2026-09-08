package com.is2.tinder.dtos;

import com.is2.tinder.entities.Foto;

public class FotoDTO {
    private String id;
    private String nombre;
    private String mime;
    private byte[] contenido;

    public static FotoDTO fromEntity(Foto foto) {
        if (foto == null) return null;
        FotoDTO dto = new FotoDTO();
        dto.id = foto.getId();
        dto.nombre = foto.getNombre();
        dto.mime = foto.getMime();
        dto.contenido = foto.getContenido();
        return dto;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMime() { return mime; }
    public byte[] getContenido() { return contenido; }
}