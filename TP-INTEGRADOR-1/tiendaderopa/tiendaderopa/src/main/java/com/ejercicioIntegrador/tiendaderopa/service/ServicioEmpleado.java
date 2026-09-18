package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Empleado;
import com.ejercicioIntegrador.tiendaderopa.model.Empresa;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpleado;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioEmpresa;
import com.ejercicioIntegrador.tiendaderopa.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 * Todas las reglas de negocio de Empleado viven acá (en el service).
 * La entidad Empleado es solo persistencia (hereda de Persona), el
 * controller solo orquesta HTTP.
 */
@Service
public class ServicioEmpleado {

    @Autowired
    private RepositorioEmpleado repositorio;

    @Autowired
    private RepositorioEmpresa empresaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

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

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 3) {
            throw new MiException("El nombre debe tener al menos 3 caracteres");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new MiException("El apellido no puede estar vacío");
        }
        if (apellido.trim().length() < 3) {
            throw new MiException("El apellido debe tener al menos 3 caracteres");
        }
        if (fechaNacimiento == null) {
            throw new MiException("Debe indicar la fecha de nacimiento");
        }
        if (fechaNacimiento.after(new Date())) {
            throw new MiException("La fecha de nacimiento no puede ser futura");
        }
        if (calcularEdad(fechaNacimiento) < 18) {
            throw new MiException("El empleado debe ser mayor de edad");
        }
        if (tipoDocumento == null) {
            throw new MiException("Debe indicar el tipo de documento");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new MiException("El número de documento no puede estar vacío");
        }
        if (tipoDocumento == TipoDocumento.DNI && !documento.trim().matches("\\d{7,8}")) {
            throw new MiException("El DNI debe tener 7 u 8 dígitos numéricos");
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
        Empleado empleado = buscarEmpleado(idEmpleado);

        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new MiException("No existe un usuario con id: " + idUsuario));

        // Persona.asignarUsuario ya se encarga de la relación bidireccional
        empleado.asignarUsuario(usuario);

        this.repositorio.save(empleado);
    }
}