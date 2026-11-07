package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;

public interface RepositorioVigenciaPrecio extends BaseRepository<VigenciaPrecio> {

    List<VigenciaPrecio> findByProducto_IdOrderByFechaDesdeDesc(String idProducto);

    List<VigenciaPrecio> findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc(String idProducto);
}
