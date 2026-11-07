package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface RepositorioProducto extends BaseRepository<Producto> {

    Optional<Producto> findFirstByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    Optional<Producto> findFirstByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Producto> findByEnOfertaTrueAndEliminadoFalse();

    /** Productos (activos o dados de baja) que tienen asignada la imagen indicada. */
    List<Producto> findByImagenes_Id(String idImagen);
}
