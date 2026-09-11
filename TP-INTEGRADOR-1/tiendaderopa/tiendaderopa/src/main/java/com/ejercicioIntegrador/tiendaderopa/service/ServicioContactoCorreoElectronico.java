package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoCorreoElectronico;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioContactoCorreoElectronico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ServicioContactoCorreoElectronico {

    // Reglas de negocio centralizadas acá, como pide el enunciado
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    private RepositorioContactoCorreoElectronico repositorio;

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Transactional
    public ContactoCorreoElectronico crearContactoCorreoElectronico(
            String email, TipoContacto tipoContacto, String observacion, String personaId) throws MiException {

        validar(email, tipoContacto, observacion);

        Persona persona = personaRepositorio.findById(personaId)
                .orElseThrow(() -> new MiException("No existe una persona con id: " + personaId));

        ContactoCorreoElectronico contacto =
                new ContactoCorreoElectronico(email.trim(), tipoContacto, observacion, persona);
        return this.repositorio.save(contacto);
    }

    public void validar(String email, TipoContacto tipoContacto, String observacion) throws MiException {
        if (email == null || email.trim().isEmpty()) {
            throw new MiException("El email no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new MiException("El email ingresado no es válido");
        }
        if (tipoContacto == null) {
            throw new MiException("Debe indicar el tipo de contacto (PERSONAL, LABORAL o EMPRESA)");
        }
        if (observacion != null && observacion.length() > 255) {
            throw new MiException("La observación no puede superar los 255 caracteres");
        }
    }

    @Transactional
    public ContactoCorreoElectronico modificarContactoCorreoElectronico(
            String id, String email, TipoContacto tipoContacto, String observacion) throws MiException {

        validar(email, tipoContacto, observacion);

        ContactoCorreoElectronico contacto = this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No existe un contacto de correo electrónico con id: " + id));

        contacto.setEmail(email.trim());
        contacto.setTipoContacto(tipoContacto);
        contacto.setObservacion(observacion);

        return this.repositorio.save(contacto);
    }

    @Transactional
    public List<ContactoCorreoElectronico> listarContactoCorreoElectronico() {
        return this.repositorio.findAll();
    }

    @Transactional
    public List<ContactoCorreoElectronico> listarContactoCorreoElectronicoActivo() {
        return this.repositorio.findByEliminadoFalse();
    }
}
