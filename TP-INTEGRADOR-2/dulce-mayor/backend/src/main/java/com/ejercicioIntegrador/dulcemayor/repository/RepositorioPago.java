package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPago extends BaseRepository<Pago> {

    Optional<Pago> findByPedido_IdAndEliminadoFalse(String idPedido);

    Optional<Pago> findByIdOrdenMpAndEliminadoFalse(String idOrdenMp);

    Optional<Pago> findByIdPagoMpAndEliminadoFalse(String idPagoMp);

    List<Pago> findByPedido_IdInAndEliminadoFalse(Collection<String> idsPedido);
}
