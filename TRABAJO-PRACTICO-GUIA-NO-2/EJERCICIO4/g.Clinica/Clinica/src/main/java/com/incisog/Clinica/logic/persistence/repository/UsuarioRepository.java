package com.incisog.Clinica.logic.persistence.repository;

import com.incisog.Clinica.models.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends BaseRepositoryTemplate<Usuario> {

    boolean existsByNombreUsuarioAndEliminadoFalse(String nombreUsuario);

    Optional<Usuario> findByNombreUsuarioAndEliminadoFalse(String nombreUsuario);
}
