package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    @Query("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario")
    Usuario buscarPorNombreUsuario(@Param("nombreUsuario") String nombreUsuario);

    Optional<Usuario> findByPersonaId(String personaId);
}
