package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.Categoria;
import com.uncuyo.tp1_ej4.repositories.RepositorioCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioCategoria extends ServicioBase<Categoria> {
    @Autowired
    private RepositorioCategoria repositorio;

    @Override
    protected RepositorioCategoria getRepositorio() {
        return repositorio;
    }
}
