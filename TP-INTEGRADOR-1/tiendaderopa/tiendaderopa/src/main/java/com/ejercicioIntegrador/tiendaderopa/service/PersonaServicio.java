package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class PersonaServicio {

    @Autowired
    private PersonaRepositorio repositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public Persona crearPersona(String nombre, String apellido, Date fechaNacimiento, TipoDocumento tipoDocumento,
                                 String documento) throws MiException {

        validar(nombre, apellido, fechaNacimiento, tipoDocumento, documento);

        Persona persona = new Persona(nombre.trim(), apellido.trim(), fechaNacimiento, documento.trim(), tipoDocumento);
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

    @Transactional
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
        buscarPersona(id); // valida que exista antes de borrar
        this.repositorio.deleteById(id);
    }

    @Transactional
    public List<Persona> listarPersona() {
        return this.repositorio.findAll();
    }


    @Transactional
    public void asignarUsuario(String idPersona, String idUsuario) throws MiException {
        Persona persona = buscarPersona(idPersona);

        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new MiException("No existe un usuario con id: " + idUsuario));

        // Regla de negocio: no permitir robarle un usuario a otra persona
        if (usuario.getPersona() != null && !usuario.getPersona().getId().equals(idPersona)) {
            throw new MiException("El usuario ya está asociado a otra persona");
        }

        persona.setUsuario(usuario);
        usuario.setPersona(persona);

        this.repositorio.save(persona);
    }

    //Desvincula el Usuario de una Persona.
    @Transactional
    public void removerUsuario(String idPersona) throws MiException {
        Persona persona = buscarPersona(idPersona);

        if (persona.getUsuario() != null) {
            persona.getUsuario().setPersona(null);
            persona.setUsuario(null);
            this.repositorio.save(persona);
        }
    }
}