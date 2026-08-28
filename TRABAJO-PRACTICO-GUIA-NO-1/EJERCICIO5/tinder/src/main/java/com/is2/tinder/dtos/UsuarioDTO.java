package com.is2.tinder.dtos;

import com.is2.tinder.entities.Usuario;

public class UsuarioDTO {
    private String id;
    private String nombre;
    private String apellido;
    private String mail;
    private ZonaDTO zona;
    private FotoDTO foto;

    public static UsuarioDTO fromEntity(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = usuario.getId();
        dto.nombre = usuario.getNombre();
        dto.apellido = usuario.getApellido();
        dto.mail = usuario.getMail();
        dto.zona = usuario.getZona() == null ? null : ZonaDTO.fromEntity(usuario.getZona());
        dto.foto = FotoDTO.fromEntity(usuario.getFoto());
        return dto;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getMail() { return mail; }
    public ZonaDTO getZona() { return zona; }
    public FotoDTO getFoto() { return foto; }
}