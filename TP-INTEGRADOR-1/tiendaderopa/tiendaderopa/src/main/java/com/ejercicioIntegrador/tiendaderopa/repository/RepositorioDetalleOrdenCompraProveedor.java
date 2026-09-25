package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** ARCHIVO NUEVO. Solo ServicioOrdenCompraProveedor lo inyecta. */
@Repository
public interface RepositorioDetalleOrdenCompraProveedor extends JpaRepository<DetalleOrdenCompraProveedor, String> {
    List<DetalleOrdenCompraProveedor> findByOrdenCompraProveedor_Id(String idOrdenCompraProveedor);
}
