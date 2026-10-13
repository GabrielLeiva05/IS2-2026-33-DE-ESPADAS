package com.uncuyo.tp1_ej4.services;

import com.uncuyo.tp1_ej4.entities.Videojuego;
import com.uncuyo.tp1_ej4.repositories.RepositorioVideojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServicioVideojuego extends ServicioBase<Videojuego> {
    @Autowired
    private RepositorioVideojuego repositorio;

    @Override
    protected JpaRepository<Videojuego, Long> getRepositorio() {
        return repositorio;
    }

    @Override
    protected void asignarId(Videojuego videojuego, long id) {
        videojuego.setId(id);
    }

    @Override
    protected void marcarComoInactivo(Videojuego videojuego) {
        videojuego.setActivo(false);
    }

    /*   Metodos nuevos   */

    @Transactional
    public List<Videojuego> findAllByActivo() {
        return this.repositorio.findAllByActivo();
    }

    @Transactional
    public Videojuego findByIdAndActivo(long id) {
        return this.repositorio.findByIdAndActivo(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe un videojuego activo con id " + id));
    }

    @Transactional
    public List<Videojuego> findByTitle(String q) {
        return this.repositorio.findByTitle(q);
    }
}
