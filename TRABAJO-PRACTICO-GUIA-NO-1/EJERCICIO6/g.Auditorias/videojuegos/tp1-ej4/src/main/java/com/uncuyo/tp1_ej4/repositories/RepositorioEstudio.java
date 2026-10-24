package com.uncuyo.tp1_ej4.repositories;

import com.uncuyo.tp1_ej4.entities.Estudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioEstudio extends JpaRepository<Estudio, Long> {
}
