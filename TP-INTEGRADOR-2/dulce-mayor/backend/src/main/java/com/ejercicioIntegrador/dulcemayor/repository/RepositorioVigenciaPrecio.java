package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioVigenciaPrecio extends BaseRepository<VigenciaPrecio> {

    List<VigenciaPrecio> findByProducto_IdOrderByFechaDesdeDesc(String idProducto);

    List<VigenciaPrecio> findByProducto_IdAndEliminadoFalseOrderByFechaDesdeDesc(String idProducto);
}
