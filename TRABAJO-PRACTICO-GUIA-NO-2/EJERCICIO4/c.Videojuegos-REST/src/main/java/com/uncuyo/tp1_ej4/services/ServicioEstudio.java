package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.repositories.RepositorioEstudio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioEstudio extends ServicioBase<Estudio> {
    @Autowired
    private RepositorioEstudio repositorio;

    @Override
    protected RepositorioEstudio getRepositorio() {
        return repositorio;
    }
}
