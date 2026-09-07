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
public class ServicioSubCategoria implements ServicioBase<SubCategoria> {
    @Autowired
    private RepositorioSubCategoria repositorio;

    @Override
    @Transactional
    public List<SubCategoria> findAll() throws Exception {
        try {
            List<SubCategoria> subCategorias = this.repositorio.findAll();
            return subCategorias;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public SubCategoria findById(long id) throws Exception {
        try {
            Optional<SubCategoria> opt = this.repositorio.findById(id);
            return opt.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public SubCategoria saveOne(SubCategoria entity) throws Exception {
        try {
            SubCategoria subCategoria = this.repositorio.save(entity);
            return subCategoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public SubCategoria updateOne(SubCategoria entity, long id) throws Exception {
        try {
            Optional<SubCategoria> opt = this.repositorio.findById(id);
            SubCategoria subCategoria = opt.get();
            subCategoria = this.repositorio.save(entity);
            return subCategoria;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteById(long id) throws Exception {
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
}
