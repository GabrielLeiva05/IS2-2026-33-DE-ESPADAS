package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioOrdenCompra extends JpaRepository<OrdenCompra, String> {

    List<OrdenCompra> findByEliminadoFalse();

    List<OrdenCompra> findByEstadoOrdenCompraAndEliminadoFalse(EstadoOrdenCompra estado);
}