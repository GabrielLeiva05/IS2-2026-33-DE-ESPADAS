package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PersonaServicio {

    @Autowired
    private PersonaRepositorio repositorio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Transactional
    public Persona crearPersona(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                String documento) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento);

        Persona persona = new Persona(nombre.trim(), apellido.trim(), fechaNacimiento, documento.trim(), tipoDocumento);
        return this.repositorio.save(persona);
    }

    @Transactional
    public Persona crearPersona(Direccion direccion, String documento, TipoDocumento tipoDocumento,
                                String nombre, Date fechaNacimiento, String apellido) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento);

        Persona persona = new Persona(nombre.trim(), apellido.trim(), fechaNacimiento, documento.trim(), tipoDocumento);
        if (direccion != null) {
            List<Direccion> dir = new ArrayList<>();
            dir.add(direccion);
            persona.setDirecciones(dir);
        }
        return this.repositorio.save(persona);
    }

    public void validar(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                        String documento) throws MiException {

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
        if (tipoDocumento == null) {
            throw new MiException("Debe indicar el tipo de documento");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new MiException("El número de documento no puede estar vacío");
        }
        if (tipoDocumento == TipoDocumento.DNI && !documento.trim().matches("\\d{7,8}")) {
            throw new MiException("El DNI debe tener 7 u 8 dígitos numéricos");
        }
    }

    @Transactional(readOnly = true)
    public Persona buscarPersona(String id) throws MiException {
        return this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe una persona con id: " + id));
    }

    @Transactional
    public Persona modificarPersona(String id, String nombre, String apellido, Date fechaNacimiento,
                                    TipoDocumento tipoDocumento, String documento) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento);

        Persona persona = buscarPersona(id);

        persona.setNombre(nombre.trim());
        persona.setApellido(apellido.trim());
        persona.setFechaNacimiento(fechaNacimiento);
        persona.setTipoDocumento(tipoDocumento);
        persona.setDocumento(documento.trim());

        return this.repositorio.save(persona);
    }

    @Transactional
    public void eliminarPersona(String id) throws MiException {
        Persona persona = buscarPersona(id);
        persona.setEliminado(true);
        this.repositorio.save(persona);
    }

    @Transactional(readOnly = true)
    public List<Persona> listarPersona() {
        return this.repositorio.findByEliminadoFalse();
    }

    @Transactional
    public void asignarUsuario(String idPersona, String idUsuario) throws MiException {
        Persona persona = buscarPersona(idPersona);
        Usuario usuario = usuarioServicio.buscarPorId(idUsuario);

        if (usuario.getPersona() != null && !usuario.getPersona().getId().equals(idPersona)) {
            throw new MiException("El usuario ya está asociado a otra persona");
        }

        usuarioServicio.desactivarUsuarioActivoDePersona(persona.getId(), usuario.getId());
        usuarioServicio.asociarAPersona(usuario, persona);

        if (!persona.getUsuarios().contains(usuario)) {
            persona.getUsuarios().add(usuario);
        }

        this.repositorio.save(persona);
    }

    @Transactional
    public void removerUsuario(String idPersona) throws MiException {
        Persona persona = buscarPersona(idPersona);
        usuarioServicio.desactivarTodosLosUsuariosDePersona(persona);
        this.repositorio.save(persona);
    }
}