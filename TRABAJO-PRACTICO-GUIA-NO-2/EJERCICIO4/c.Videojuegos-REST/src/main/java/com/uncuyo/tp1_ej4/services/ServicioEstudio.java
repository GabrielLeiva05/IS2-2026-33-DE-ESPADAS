package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.Estudio;
import com.uncuyo.tp1_ej4.repositories.RepositorioEstudio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicioEstudio extends ServicioBase<Estudio> {
    @Autowired
    private RepositorioEstudio repositorio;

    @Override
    protected JpaRepository<Estudio, Long> getRepositorio() {
        return repositorio;
    }

    @Override
    protected void asignarId(Estudio estudio, long id) {
        estudio.setId(id);
    }

    @Override
    protected void marcarComoInactivo(Estudio estudio) {
        estudio.setActivo(false);
    }
}
