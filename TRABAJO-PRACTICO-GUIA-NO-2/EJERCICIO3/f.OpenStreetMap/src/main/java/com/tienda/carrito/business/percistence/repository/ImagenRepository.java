package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Imagen;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenRepository extends BaseRepository<Imagen, String> {

    /** Solo los ids (sin los bytes) de las imagenes activas de un articulo: sirve para armar el catalogo liviano. */
    @Query("SELECT i.id FROM Articulo a JOIN a.imagenes i "
            + "WHERE a.id = :articuloId AND i.eliminado = FALSE ORDER BY i.id")
    List<String> listarIdsActivosPorArticulo(@Param("articuloId") String articuloId);
}
