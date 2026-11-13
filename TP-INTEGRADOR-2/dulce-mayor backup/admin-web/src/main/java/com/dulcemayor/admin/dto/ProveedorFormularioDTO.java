package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Formulario de alta y modificación de proveedores. */
@Getter
@Setter
@NoArgsConstructor
public class ProveedorFormularioDTO implements FormularioConUbicacion {

    private String id;
    private String razonSocial;
    private String cuit;
    private String calle;
    private String numeracion;
    private String barrio;
    private String idPais;
    private String idProvincia;
    private String idDepartamento;
    private String idLocalidad;

    public boolean isNuevo() {
        return id == null || id.isBlank();
    }

    public static ProveedorFormularioDTO desde(ProveedorDTO p) {
        ProveedorFormularioDTO f = new ProveedorFormularioDTO();
        f.setId(p.getId());
        f.setRazonSocial(p.getRazonSocial());
        f.setCuit(p.getCuit());
        if (p.getDireccion() != null) {
            DireccionDTO d = p.getDireccion();
            f.setCalle(d.getCalle());
            f.setNumeracion(d.getNumeracion());
            f.setBarrio(d.getBarrio());
            f.setIdPais(d.getIdPais());
            f.setIdProvincia(d.getIdProvincia());
            f.setIdDepartamento(d.getIdDepartamento());
            f.setIdLocalidad(d.getIdLocalidad());
        }
        return f;
    }
}
