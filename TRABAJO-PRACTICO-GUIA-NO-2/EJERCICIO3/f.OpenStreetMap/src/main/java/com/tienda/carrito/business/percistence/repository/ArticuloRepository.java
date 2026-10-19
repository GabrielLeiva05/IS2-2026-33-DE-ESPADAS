package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Articulo;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticuloRepository extends BaseRepository<Articulo, String> {
}
