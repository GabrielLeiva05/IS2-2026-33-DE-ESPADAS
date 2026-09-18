package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empleado;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpleado;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ServicioEmpleado {

    @Autowired
    private RepositorioEmpleado repositorio;

    @Autowired
    private RepositorioEmpresa empresaRepositorio;

    @Autowired
    private PersonaServicio personaServicio;

    @Transactional
    public Empleado crearEmpleado(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                   String documento, TipoEmpleado tipoEmpleado, String idEmpresa) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento, tipoEmpleado);

        Empresa empresa = null;
        if (idEmpresa != null && !idEmpresa.trim().isEmpty()) {
            empresa = empresaRepositorio.findById(idEmpresa)
                    .orElseThrow(() -> new MiException("No existe una empresa con id: " + idEmpresa));
        }

        Empleado empleado = new Empleado(nombre.trim(), apellido.trim(), fechaNacimiento, documento.trim(),
                tipoDocumento, tipoEmpleado, empresa);

        return this.repositorio.save(empleado);
    }

    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                         String documento, TipoEmpleado tipoEmpleado) throws MiException {

        // Validaciones de los campos heredados de Persona: se delegan para
        // no duplicar la lógica que ya vive en PersonaServicio.
        personaServicio.validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento);

        // De acá para abajo, reglas propias de Empleado.
        if (calcularEdad(fechaNacimiento) < 18) {
            throw new MiException("El empleado debe ser mayor de edad");
        }
        if (tipoEmpleado == null) {
            throw new MiException("Debe indicar el tipo de empleado (ADMINISTRATIVO o JEFE)");
        }
    }

    private int calcularEdad(Date fechaNacimiento) {
        Calendar nacimiento = Calendar.getInstance();
        nacimiento.setTime(fechaNacimiento);
        Calendar hoy = Calendar.getInstance();

        int edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }
        return edad;
    }

    @Transactional
    public Empleado buscarEmpleado(String id) throws MiException {
        return this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe un empleado con id: " + id));
    }

    @Transactional
    public Empleado modificarEmpleado(String id, String nombre, String apellido, Date fechaNacimiento,
                                       TipoDocumento tipoDocumento, String documento, TipoEmpleado tipoEmpleado,
                                       String idEmpresa) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento, tipoEmpleado);

        Empleado empleado = buscarEmpleado(id);

        Empresa empresa = null;
        if (idEmpresa != null && !idEmpresa.trim().isEmpty()) {
            empresa = empresaRepositorio.findById(idEmpresa)
                    .orElseThrow(() -> new MiException("No existe una empresa con id: " + idEmpresa));
        }

        empleado.setNombre(nombre.trim());
        empleado.setApellido(apellido.trim());
        empleado.setFechaNacimiento(fechaNacimiento);
        empleado.setTipoDocumento(tipoDocumento);
        empleado.setDocumento(documento.trim());
        empleado.setTipoEmpleado(tipoEmpleado);
        empleado.setEmpresa(empresa);

        return this.repositorio.save(empleado);
    }

    @Transactional
    public void eliminarEmpleado(String id) throws MiException {
        Empleado empleado = buscarEmpleado(id);
        // baja lógica
        empleado.setEliminado(true);
        this.repositorio.save(empleado);
    }

    @Transactional
    public List<Empleado> listarEmpleado() {
        return this.repositorio.findAll();
    }

    @Transactional
    public List<Empleado> listarEmpleadoActivo() {
        return this.repositorio.findByEliminadoFalse();
    }

    @Transactional
    public void asociarEmpleadoUsuario(String idEmpleado, String idUsuario) throws MiException {
        // Se confirma que el id corresponde a un Empleado (da un mensaje de
        // error más claro que el genérico de Persona si no existe).
        buscarEmpleado(idEmpleado);

        // Empleado ES una Persona (misma fila, misma PK), así que asociar
        // el Usuario es una operación de Persona: se delega en
        // PersonaServicio en vez de reimplementarla acá.
        personaServicio.asignarUsuario(idEmpleado, idUsuario);
    }

    @Transactional
    public void desasociarEmpleadoUsuario(String idEmpleado) throws MiException {
        buscarEmpleado(idEmpleado);
        personaServicio.removerUsuario(idEmpleado);
    }
}