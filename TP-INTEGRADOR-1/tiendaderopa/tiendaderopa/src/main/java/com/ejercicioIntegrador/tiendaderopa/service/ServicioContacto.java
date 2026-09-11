package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Contacto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioContacto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ServicioContacto {

    @Autowired
    private RepositorioContacto repositorio;

    @Transactional
    public Contacto buscarContacto(String id) throws MiException {
        Optional<Contacto> opt = this.repositorio.findById(id);
        if (opt.isEmpty()) {
            throw new MiException("No existe un contacto con id: " + id);
        }
        return opt.get();
    }

    @Transactional
    public void eliminarContacto(String id) throws MiException {
        Contacto contacto = buscarContacto(id);
        // baja lógica 
        contacto.setEliminado(true);
        this.repositorio.save(contacto);
    }
}
