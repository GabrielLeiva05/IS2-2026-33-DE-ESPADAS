package com.dulcemayor.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo del formulario de alta y modificación de empleados (HU-07).
 * Se mantiene plano para que Thymeleaf lo enlace con th:field; el servicio lo convierte en EmpleadoSolicitudDTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class EmpleadoFormularioDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String fechaNacimiento;
    private String tipoDocumento = "DNI";
    private String numeroDocumento;
    private String correo;
    private String rol;
    private String legajo;
    private String fechaIngreso;
    private String zona;
    private String vehiculo;
    private String patente;
    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;
    private String idPais;
    private String idProvincia;
    private String idDepartamento;
    private String idLocalidad;

    public boolean isNuevo() {
        return id == null || id.isBlank();
    }

    /** Carga el formulario con los datos actuales de un empleado para editarlo. */
    public static EmpleadoFormularioDTO desde(EmpleadoDTO empleado) {
        EmpleadoFormularioDTO formulario = new EmpleadoFormularioDTO();
        formulario.setId(empleado.getId());
        formulario.setNombre(empleado.getNombre());
        formulario.setApellido(empleado.getApellido());
        formulario.setFechaNacimiento(empleado.getFechaNacimiento());
        formulario.setTipoDocumento(empleado.getTipoDocumento());
        formulario.setNumeroDocumento(empleado.getNumeroDocumento());
        formulario.setCorreo(empleado.getCorreo());
        formulario.setRol(empleado.getRol());
        formulario.setLegajo(empleado.getLegajo());
        formulario.setFechaIngreso(empleado.getFechaIngreso());
        formulario.setZona(empleado.getZona());
        formulario.setVehiculo(empleado.getVehiculo());
        formulario.setPatente(empleado.getPatente());
        DireccionDTO direccion = empleado.getDireccion();
        if (direccion != null) {
            formulario.setCalle(direccion.getCalle());
            formulario.setNumeracion(direccion.getNumeracion());
            formulario.setBarrio(direccion.getBarrio());
            formulario.setManzanaPiso(direccion.getManzanaPiso());
            formulario.setCasaDepartamento(direccion.getCasaDepartamento());
            formulario.setReferencia(direccion.getReferencia());
            formulario.setIdPais(direccion.getIdPais());
            formulario.setIdProvincia(direccion.getIdProvincia());
            formulario.setIdDepartamento(direccion.getIdDepartamento());
            formulario.setIdLocalidad(direccion.getIdLocalidad());
        }
        return formulario;
    }
}
