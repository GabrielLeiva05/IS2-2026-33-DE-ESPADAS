package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.repository.LocalidadRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocalidadServicio {

    private final LocalidadRepositorio localidadRepositorio;

    public LocalidadServicio(LocalidadRepositorio localidadRepositorio) {
        this.localidadRepositorio = localidadRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Localidad> listarTodas() {
        return localidadRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Localidad buscarPorId(String id) throws MiException {
        return localidadRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró la localidad solicitada"));
    }

    @Transactional
    public Localidad crearLocalidad(String nombre, Departamento departamento) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre de la localidad no puede estar vacío");
        }
        if (departamento == null) {
            throw new MiException("Debe asociar un departamento a la localidad");
        }

        Localidad localidad = new Localidad();
        localidad.setNombre(nombre.trim());
        localidad.setDepartamento(departamento);
        return localidadRepositorio.save(localidad);
    }

    @Transactional
    public void eliminar(String id) throws MiException {
        Localidad localidad = buscarPorId(id);
        localidadRepositorio.delete(localidad);
    }
}