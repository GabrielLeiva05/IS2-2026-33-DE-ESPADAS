package com.ejercicioIntegrador.tiendaderopa.controller;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoSucursal;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.TipoPago;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone los enums a las vistas para armar los <select> de los ABMs del dashboard,
 * tanto cuando se carga /admin/dashboard como cuando un controlador devuelve panel.html
 * para editar un registro.
 */
@ControllerAdvice(annotations = Controller.class)
public class PanelAtributosAdvice {

    @ModelAttribute("tiposDocumento")
    public TipoDocumento[] tiposDocumento() {
        return TipoDocumento.values();
    }

    @ModelAttribute("tiposEmpleado")
    public TipoEmpleado[] tiposEmpleado() {
        return TipoEmpleado.values();
    }

    @ModelAttribute("tiposSucursal")
    public TipoSucursal[] tiposSucursal() {
        return TipoSucursal.values();
    }

    @ModelAttribute("tiposContacto")
    public TipoContacto[] tiposContacto() {
        return TipoContacto.values();
    }

    @ModelAttribute("tiposTelefono")
    public TipoTelefono[] tiposTelefono() {
        return TipoTelefono.values();
    }

    @ModelAttribute("rolesUsuario")
    public RolUsuario[] rolesUsuario() {
        return RolUsuario.values();
    }

    @ModelAttribute("tiposPago")
    public TipoPago[] tiposPago() {
        return TipoPago.values();
    }

    @ModelAttribute("estadosFactura")
    public EstadoFactura[] estadosFactura() {
        return EstadoFactura.values();
    }
}
