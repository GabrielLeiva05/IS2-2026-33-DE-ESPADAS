package com.uncuyo.tp1_ej4.repositories;

import com.uncuyo.tp1_ej4.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioRol extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
