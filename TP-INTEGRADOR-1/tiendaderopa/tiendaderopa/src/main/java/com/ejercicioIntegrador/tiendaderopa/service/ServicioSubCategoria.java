package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioSubCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public SubCategoria crear(String nombre, Categoria categoria) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("El nombre de la subcategoría no puede estar vacío");
        }
        if (categoria == null) {
            throw new Exception("Debe seleccionar una categoría");
        }
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setNombre(nombre.trim());
        subCategoria.setActivo(true);
        subCategoria.setCategoria(categoria);
        return this.repositorio.save(subCategoria);
    }

    @Transactional
    public SubCategoria modificar(String id, String nombre, Categoria categoria, boolean activo) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("El nombre de la subcategoría no puede estar vacío");
        }
        if (categoria == null) {
            throw new Exception("Debe seleccionar una categoría");
        }
        SubCategoria subCategoria = findById(id);
        subCategoria.setNombre(nombre.trim());
        subCategoria.setCategoria(categoria);
        subCategoria.setActivo(activo);
        return this.repositorio.save(subCategoria);
    }

    @Transactional
    public void eliminar(String id) throws Exception {
        SubCategoria subCategoria = findById(id);
        subCategoria.setActivo(false);
        this.repositorio.save(subCategoria);
    }
}
