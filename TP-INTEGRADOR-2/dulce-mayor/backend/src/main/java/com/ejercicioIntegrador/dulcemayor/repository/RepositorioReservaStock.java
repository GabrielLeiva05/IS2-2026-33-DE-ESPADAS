package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.ReservaStock;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;

public interface RepositorioReservaStock extends BaseRepository<ReservaStock> {

    List<ReservaStock> findByPedido_IdAndEliminadoFalse(String idPedido);

    List<ReservaStock> findByEliminadoFalseAndConsumidaFalseAndFechaLiberacionIsNull();
}
