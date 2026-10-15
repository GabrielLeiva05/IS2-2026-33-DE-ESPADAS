package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Carrito;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends BaseRepository<Carrito, String> {

    /** Carrito activo (no cerrado) de un usuario, o null si no tiene. */
    @Query("SELECT c FROM Carrito c WHERE c.usuario.id = :usuarioId AND c.eliminado = FALSE")
    Carrito buscarCarritoActivoPorUsuario(@Param("usuarioId") String usuarioId);
}
