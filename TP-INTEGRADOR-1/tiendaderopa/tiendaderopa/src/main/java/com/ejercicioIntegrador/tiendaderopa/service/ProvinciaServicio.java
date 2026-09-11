package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.repository.ProvinciaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProvinciaServicio {

    private final ProvinciaRepositorio provinciaRepositorio;

    public ProvinciaServicio(ProvinciaRepositorio provinciaRepositorio) {
        this.provinciaRepositorio = provinciaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Provincia> listarTodas() {
        return provinciaRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Provincia buscarPorId(String id) throws MiException {
        return provinciaRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró la provincia solicitada"));
    }

    @Transactional
    public Provincia crearProvincia(String nombre, Pais pais) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre de la provincia no puede estar vacío");
        }
        if (pais == null) {
            throw new MiException("Debe asociar un país a la provincia");
        }

        Provincia provincia = new Provincia();
        provincia.setNombre(nombre.trim());
        provincia.setPais(pais);
        return provinciaRepositorio.save(provincia);
    }

    @Transactional
    public void eliminar(String id) throws MiException {
        Provincia provincia = buscarPorId(id);
        provinciaRepositorio.delete(provincia);
    }
}