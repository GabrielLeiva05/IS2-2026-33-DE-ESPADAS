package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Comprobante;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.Optional;

public interface RepositorioComprobante extends BaseRepository<Comprobante> {

    Optional<Comprobante> findFirstByPedido_IdAndEliminadoFalse(String idPedido);

    Optional<Comprobante> findFirstByPago_IdAndEliminadoFalse(String idPago);

    Optional<Comprobante> findFirstByNumeroAndEliminadoFalse(String numero);
}
