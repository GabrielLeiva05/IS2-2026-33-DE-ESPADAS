package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioSubCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.ejercicioIntegrador.tiendaderopa.exceptions.MiException;

@Service
public class ServicioSubCategoria {
    @Autowired
    private RepositorioSubCategoria repositorio;

    
    @Transactional
    public List<SubCategoria> findAll() throws Exception {
        try {
            List<SubCategoria> subCategorias = this.repositorio.findAll();
            return subCategorias;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public SubCategoria findById(String id) throws Exception {
        try {
            Optional<SubCategoria> opt = this.repositorio.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public SubCategoria saveOne(SubCategoria entity) throws Exception {
        try {
            SubCategoria subCategoria = this.repositorio.save(entity);
            return subCategoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public SubCategoria updateOne(SubCategoria entity, String id) throws Exception {
        try {
            Optional<SubCategoria> opt = this.repositorio.findById(id);
            SubCategoria subCategoria = opt.get();
            subCategoria = this.repositorio.save(entity);
            return subCategoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    
    @Transactional
    public boolean deleteById(String id) throws Exception {
        try {
            Optional<SubCategoria> opt = this.repositorio.findById(id);
            if (!opt.isEmpty()) {
                SubCategoria subCategoria = opt.get();
                subCategoria.setActivo(!subCategoria.isActivo());
                this.repositorio.save(subCategoria);
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
    public List<SubCategoria> listarTodas() {
        return this.repositorio.findAll();
    }

    @Transactional
    public SubCategoria crearSubCategoria(String nombre, Categoria categoria) throws MiException {
        validar(nombre, categoria);
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setNombre(nombre.trim());
        subCategoria.setCategoria(categoria);
        subCategoria.setActivo(true);
        return this.repositorio.save(subCategoria);
    }

    @Transactional
    public SubCategoria modificarSubCategoria(String id, String nombre, Categoria categoria) throws MiException {
        validar(nombre, categoria);
        SubCategoria subCategoria = this.repositorio.findById(id)
                .orElseThrow(() -> new MiException("No se encontró la subcategoría solicitada"));
        subCategoria.setNombre(nombre.trim());
        subCategoria.setCategoria(categoria);
        return this.repositorio.save(subCategoria);
    }

    private void validar(String nombre, Categoria categoria) throws MiException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiException("El nombre de la subcategoría no puede estar vacío");
        }
        if (categoria == null) {
            throw new MiException("Debe asociar una categoría a la subcategoría");
        }
    }
}
