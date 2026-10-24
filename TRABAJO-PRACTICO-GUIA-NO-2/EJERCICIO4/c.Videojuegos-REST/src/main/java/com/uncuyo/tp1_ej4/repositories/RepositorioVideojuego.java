package com.uncuyo.tp1_ej4.repositories;

import com.uncuyo.tp1_ej4.entities.Videojuego;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RepositorioVideojuego extends RepositorioBase<Videojuego> {
    @Query(value = "SELECT * FROM videojuegos  WHERE videojuegos.activo = true", nativeQuery = true)
    List<Videojuego> findAllByActivo();

    @Query(value = "SELECT * FROM videojuegos WHERE videojuegos.titulo LIKE CONCAT('%', :q, '%') AND videojuegos.activo = true", nativeQuery = true)
    List<Videojuego> findByTitle(@Param("q")String q);

}
