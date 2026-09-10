package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioProducto extends JpaRepository<Producto, String> {

    List<Producto> findByEliminadoFalse();
    Producto findByNombre(String nombre);
    Producto findByCodigo(String codigo);
}
