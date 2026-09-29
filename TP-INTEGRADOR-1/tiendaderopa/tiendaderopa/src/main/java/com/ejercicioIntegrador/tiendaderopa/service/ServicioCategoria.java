package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;

@Service
public class ServicioCategoria {
    @Autowired
    private RepositorioCategoria repositorio;

    
    @Transactional
    public List<Categoria> findAll() throws Exception {
        try {
            List<Categoria> categorias = this.repositorio.findAll();
            return categorias;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public Categoria findById(String id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorio.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public Categoria saveOne(Categoria entity) throws Exception {
        try {
            Categoria categoria = this.repositorio.save(entity);
            return categoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public Categoria updateOne(Categoria entity, String id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorio.findById(id);
            Categoria categoria = opt.get();
            categoria = this.repositorio.save(entity);
            return categoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public boolean deleteById(String id) throws Exception {
        try {
            Optional<Categoria> opt = this.repositorio.findById(id);
            if (!opt.isEmpty()) {
                Categoria categoria = opt.get();
                categoria.setActivo(!categoria.isActivo());
                this.repositorio.save(categoria);
            } else {
                throw new Exception();
            }
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // ---------- Métodos usados por el ABM del dashboard ----------

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return this.repositorio.findAll();
    }

    @Transactional
    public Categoria crearCategoria(String nombre) throws MiException {
        validarNombre(nombre);
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre.trim());
        categoria.setActivo(true);
        return this.repositorio.save(categoria);
    }

    @Transactional
    public Categoria modificarCategoria(String id, String nombre) throws MiException {
        validarNombre(nombre);
        Categoria categoria = this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró la categoría solicitada"));
        categoria.setNombre(nombre.trim());
        return this.repositorio.save(categoria);
    }

    private void validarNombre(String nombre) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre de la categoría no puede estar vacío");
        }
    }
}
