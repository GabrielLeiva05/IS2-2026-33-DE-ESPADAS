package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Detalle;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleRepository extends BaseRepository<Detalle, String> {
}
