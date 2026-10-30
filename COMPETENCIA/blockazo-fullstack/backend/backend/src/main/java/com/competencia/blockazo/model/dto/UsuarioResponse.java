package com.competencia.blockazo.model.dto;

import com.competencia.blockazo.model.entity.Usuario;
import com.competencia.blockazo.model.enums.Proveedor;
import com.competencia.blockazo.model.enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Datos del usuario que se muestran en "Mi perfil" (nunca incluye la clave). */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String dni,
        LocalDate fechaNacimiento,
        String direccion,
        String correo,
        Rol rol,
        Proveedor proveedor,
        LocalDateTime fechaAlta,
        boolean perfilCompleto,
        boolean tieneClave
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getDni(),
                u.getFechaNacimiento(), u.getDireccion(), u.getCorreo(), u.getRol(),
                u.getProveedor(), u.getFechaAlta(), u.isPerfilCompleto(), u.isTieneClave());
    }
}
