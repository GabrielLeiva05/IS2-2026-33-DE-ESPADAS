package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** ARCHIVO NUEVO. Solo ServicioOrdenCompraProveedor lo inyecta. */
@Repository
public interface RepositorioOrdenCompraProveedor extends JpaRepository<OrdenCompraProveedor, String> {
    List<OrdenCompraProveedor> findByEliminadoFalse();
    List<OrdenCompraProveedor> findByEstado(EstadoOrdenCompraProveedor estado);
}
