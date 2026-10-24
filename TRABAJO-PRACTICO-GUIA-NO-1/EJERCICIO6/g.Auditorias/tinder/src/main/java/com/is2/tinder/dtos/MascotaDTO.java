package com.is2.tinder.dtos;

import com.is2.tinder.entities.Mascota;
import com.is2.tinder.enumerations.Sexo;
import com.is2.tinder.enumerations.Tipo;

public class MascotaDTO {
    private String id;
    private String nombre;
    private Sexo sexo;
    private Tipo tipo;
    private UsuarioDTO usuario;
    private FotoDTO foto;

    public MascotaDTO() { }

    public static MascotaDTO fromEntity(Mascota mascota) {
        if (mascota == null) return null;
        MascotaDTO dto = new MascotaDTO();
        dto.id = mascota.getId();
        dto.nombre = mascota.getNombre();
        dto.sexo = mascota.getSexo();
        dto.tipo = mascota.getTipo();
        dto.usuario = UsuarioDTO.fromEntity(mascota.getUsuario());
        dto.foto = FotoDTO.fromEntity(mascota.getFoto());
        return dto;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Sexo getSexo() { return sexo; }
    public Tipo getTipo() { return tipo; }
    public UsuarioDTO getUsuario() { return usuario; }
    public FotoDTO getFoto() { return foto; }
}