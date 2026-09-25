package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioContactoTelefonico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioContactoTelefonico {

    @Autowired
    private RepositorioContactoTelefonico repositorio;

    @Autowired
    private PersonaServicio personaServicio;

    @Transactional
    public ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion, String personaId) throws MiException {

        validar(telefono, tipoTelefono, tipoContacto, observacion);
        if (personaId == null || personaId.isBlank()) {
            throw new MiException("Debe indicar una persona para el contacto");
        }

        Persona persona = personaServicio.buscarPersona(personaId);
        validarEntidad(persona, null);

        ContactoTelefonico contacto =
                new ContactoTelefonico(telefono.trim(), tipoTelefono, tipoContacto, observacion, persona);
        return this.repositorio.save(contacto);
    }

    @Transactional
    public ContactoTelefonico crearContactoTelefonico(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion, Proveedor proveedor) throws MiException {
        validar(telefono, tipoTelefono, tipoContacto, observacion);
        validarEntidad(null, proveedor);

        ContactoTelefonico contacto =
                new ContactoTelefonico(telefono.trim(), tipoTelefono, tipoContacto, observacion, proveedor);
        return this.repositorio.save(contacto);
    }

    private void validarEntidad(Persona persona, Proveedor proveedor) throws MiException {
        if ((persona == null) == (proveedor == null)) {
            throw new MiException("El contacto debe pertenecer a exactamente una Persona o un Proveedor");
        }
    }
    
    public void validar(String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion)
            throws MiException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new MiException("El teléfono no puede estar vacío");
        }
        if (!telefono.trim().matches("\\d{6,15}")) {
            throw new MiException("El teléfono debe contener entre 6 y 15 dígitos numéricos");
        }
        if (tipoTelefono == null) {
            throw new MiException("Debe indicar el tipo de teléfono (FIJO o CELULAR)");
        }
        if (tipoContacto == null) {
            throw new MiException("Debe indicar el tipo de contacto (PERSONAL, LABORAL o EMPRESA)");
        }
        if (observacion != null && observacion.length() > 255) {
            throw new MiException("La observación no puede superar los 255 caracteres");
        }
    }

    @Transactional
    public ContactoTelefonico modificarContactoTelefonico(String id, String telefono, TipoTelefono tipoTelefono, TipoContacto tipoContacto, String observacion)
            throws MiException {

        validar(telefono, tipoTelefono, tipoContacto, observacion);

        ContactoTelefonico contacto = this.repositorio.findById(id).orElseThrow(() -> new MiException("No existe un contacto telefónico con id: " + id));

        contacto.setTelefono(telefono.trim());
        contacto.setTipoTelefono(tipoTelefono);
        contacto.setTipoContacto(tipoContacto);
        contacto.setObservacion(observacion);

        return this.repositorio.save(contacto);
    }

    @Transactional
    public List<ContactoTelefonico> listarContactoTelefonico() {
        return this.repositorio.findAll();
    }

    @Transactional
    public List<ContactoTelefonico> listarContactoTelefonicoActivo() {
        return this.repositorio.findByEliminadoFalse();
    }
}
