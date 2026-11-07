package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Sucursal;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface RepositorioSucursal extends BaseRepository<Sucursal> {

    @EntityGraph(attributePaths = {"direccion", "direccion.localidad"})
    List<Sucursal> findByActivaTrueAndEliminadoFalseOrderByNombreAsc();

    @EntityGraph(attributePaths = {"direccion", "direccion.localidad"})
    Optional<Sucursal> findByIdAndEliminadoFalse(String id);

    List<Sucursal> findByEliminadoFalse();
}
