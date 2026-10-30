package com.competencia.blockazo.repository;

import com.competencia.blockazo.model.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    /** Busca por correo (username) sin importar mayusculas, incluso si esta dado de baja. */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo)")
    Optional<Usuario> buscarPorCorreo(@Param("correo") String correo);

    @Query("SELECT u FROM Usuario u WHERE u.dni = :dni")
    Optional<Usuario> buscarPorDni(@Param("dni") String dni);
}
