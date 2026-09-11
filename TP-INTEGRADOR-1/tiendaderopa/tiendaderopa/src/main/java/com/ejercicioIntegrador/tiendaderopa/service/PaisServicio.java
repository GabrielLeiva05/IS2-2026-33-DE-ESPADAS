package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.repository.PaisRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaisServicio {

    private final PaisRepositorio paisRepositorio;

    public PaisServicio(PaisRepositorio paisRepositorio) {
        this.paisRepositorio = paisRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Pais> listarTodos() {
        return paisRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Pais buscarPorId(String id) throws MiException {
        return paisRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró el país solicitado"));
    }

    @Transactional
    public Pais crearPais(String nombre) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre del país no puede estar vacío");
        }

        Pais pais = new Pais();
        pais.setNombre(nombre.trim());
        return paisRepositorio.save(pais);
    }

    @Transactional
    public void eliminar(String id) throws MiException {
        Pais pais = buscarPorId(id);
        paisRepositorio.delete(pais);
    }
}
