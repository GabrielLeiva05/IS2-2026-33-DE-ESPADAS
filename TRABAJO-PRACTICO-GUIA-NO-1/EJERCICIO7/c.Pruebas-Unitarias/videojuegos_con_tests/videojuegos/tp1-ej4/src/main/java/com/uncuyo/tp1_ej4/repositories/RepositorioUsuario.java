package com.uncuyo.tp1_ej4.repositories;

import com.uncuyo.tp1_ej4.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameAndActivo(String username, boolean activo);
}
