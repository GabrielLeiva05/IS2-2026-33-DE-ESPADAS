package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.repository.DepartamentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartamentoServicio {

    private final DepartamentoRepositorio departamentoRepositorio;

    public DepartamentoServicio(DepartamentoRepositorio departamentoRepositorio) {
        this.departamentoRepositorio = departamentoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Departamento> listarTodos() {
        return departamentoRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Departamento buscarPorId(String id) throws MiException {
        return departamentoRepositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró el departamento solicitado"));
    }

    @Transactional
    public Departamento crearDepartamento(String nombre, Provincia provincia) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre del departamento no puede estar vacío");
        }
        if (provincia == null) {
            throw new MiException("Debe asociar una provincia al departamento");
        }

        Departamento departamento = new Departamento();
        departamento.setNombre(nombre.trim());
        departamento.setProvincia(provincia);
        return departamentoRepositorio.save(departamento);
    }

    @Transactional
    public void eliminar(String id) throws MiException {
        Departamento departamento = buscarPorId(id);
        departamentoRepositorio.delete(departamento);
    }
}