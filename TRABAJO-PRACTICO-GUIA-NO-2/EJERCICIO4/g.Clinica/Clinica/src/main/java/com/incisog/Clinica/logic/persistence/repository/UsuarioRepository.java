package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<Usuario> findByEliminadoFalse();

    boolean existsByNombreUsuarioAndEliminadoFalse(String nombreUsuario);

    // Para el login: busca el usuario activo con ese nombre (puede no existir → Optional vacío).
    Optional<Usuario> findByNombreUsuarioAndEliminadoFalse(String nombreUsuario);

}
