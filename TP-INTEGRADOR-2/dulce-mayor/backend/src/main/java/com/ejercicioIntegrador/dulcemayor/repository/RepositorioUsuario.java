package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioUsuario extends BaseRepository<Usuario> {

    Optional<Usuario> findFirstByNombreUsuarioIgnoreCaseAndEliminadoFalse(String nombreUsuario);

    /** Todas las cuentas con ese correo, incluidas las dadas de baja. */
    List<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);
}
