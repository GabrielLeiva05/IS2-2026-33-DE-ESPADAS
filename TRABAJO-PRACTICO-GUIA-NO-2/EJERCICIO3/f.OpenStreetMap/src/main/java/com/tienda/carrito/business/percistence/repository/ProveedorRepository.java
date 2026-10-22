package com.tienda.carrito.business.percistence.repository;

import com.tienda.carrito.business.domain.entity.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends BaseRepository<Proveedor, String> {

    /** Proveedor activo con ese nombre (sin distinguir mayusculas) o null. */
    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) = LOWER(:nombre) AND p.eliminado = FALSE")
    Proveedor buscarProveedorPorNombre(@Param("nombre") String nombre);
}
