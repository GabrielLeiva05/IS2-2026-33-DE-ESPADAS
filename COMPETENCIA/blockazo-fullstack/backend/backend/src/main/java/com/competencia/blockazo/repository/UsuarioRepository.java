package com.competencia.blockazo.repository;

import com.competencia.blockazo.model.entity.Usuario;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Hereda de BaseRepository (patron Template), que a su vez extiende JpaRepository.
 * Las consultas son metodos derivados de Spring Data JPA (sin @Query manual).
 */
@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    /** Busca por correo (username) sin importar mayusculas, incluso si esta dado de baja. */
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByDni(String dni);
}
