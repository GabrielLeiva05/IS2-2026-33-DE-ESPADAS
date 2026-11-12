package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Proveedor;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioProveedor extends BaseRepository<Proveedor> {

    Optional<Proveedor> findFirstByCuitAndEliminadoFalse(String cuit);
}
