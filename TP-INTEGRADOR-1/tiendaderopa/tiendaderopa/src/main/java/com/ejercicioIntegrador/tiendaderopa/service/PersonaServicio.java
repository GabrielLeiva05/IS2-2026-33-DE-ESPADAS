package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PersonaServicio {

    @Autowired
    private PersonaRepositorio personaRepositorio;

    public Persona crearPersona(Direccion direccion,
                         String documento,
                         TipoDocumento tipoDocumento,
                         String nombre,
                         Date fechaNacimiento,
                         String apellido){
        Persona persona = new Persona();
        persona.setDocumento(documento);
        persona.setNombre(nombre);
        persona.setApellido(apellido);
        persona.setFechaNacimiento(fechaNacimiento);
        List<Direccion> dir = new ArrayList<>();
        dir.add(direccion);
        persona.setDirecciones(dir);
        personaRepositorio.save(persona);
        return persona;
    }
}
