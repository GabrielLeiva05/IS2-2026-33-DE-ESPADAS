package com.incisog.Clinica.logic.dto.adapter;

import com.incisog.Clinica.logic.dto.UsuarioDTO;
import com.incisog.Clinica.models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAdapter extends BaseAdapter<Usuario, UsuarioDTO> {

    @Override
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        // la clave NO se copia: hacia afuera nunca viaja
        return dto;
    }

    @Override
    public Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setClave(dto.getClave()); // hacia adentro sí (alta, cambio de clave)
        return usuario;
    }
}