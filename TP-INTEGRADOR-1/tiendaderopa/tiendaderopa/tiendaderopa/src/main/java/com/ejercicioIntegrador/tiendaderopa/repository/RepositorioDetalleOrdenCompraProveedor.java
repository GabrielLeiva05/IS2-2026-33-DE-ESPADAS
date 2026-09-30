package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioDetalleOrdenCompraProveedor extends JpaRepository<DetalleOrdenCompraProveedor, String> {
    List<DetalleOrdenCompraProveedor> findByOrdenCompraProveedor_Id(String idOrdenCompraProveedor);

    /**
     * Todas las compras activas hechas a proveedores por un producto dado,
     * de la más económica a la más cara. El primer elemento de la lista
     * es el proveedor "recomendado" para reponer ese producto.
     */
    @Query("SELECT d FROM DetalleOrdenCompraProveedor d " +
            "JOIN FETCH d.producto p " +
            "JOIN FETCH d.ordenCompraProveedor oc " +
            "JOIN FETCH oc.proveedor prov " +
            "WHERE d.eliminado = false " +
            "AND oc.eliminado = false " +
            "AND p.id = :idProducto " +
            "ORDER BY d.precioCompra ASC")
    List<DetalleOrdenCompraProveedor> buscarPorProductoOrdenadoPorPrecio(@Param("idProducto") String idProducto);
}
