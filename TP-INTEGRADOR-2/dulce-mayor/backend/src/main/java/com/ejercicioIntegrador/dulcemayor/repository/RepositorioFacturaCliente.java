package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioFacturaCliente extends BaseRepository<FacturaCliente> {

    /** Última factura de cliente emitida: la numeración es correlativa. */
    Optional<FacturaCliente> findTopByOrderByNumeroFacturaDesc();

    boolean existsByPedido_Id(String idPedido);

    Optional<FacturaCliente> findFirstByPedido_IdAndEliminadoFalse(String idPedido);

    List<FacturaCliente> findByPedido_IdInAndEliminadoFalse(Collection<String> idsPedido);

    List<FacturaCliente> findByEliminadoFalseOrderByNumeroFacturaDesc();

    /** listarFacturaPorCliente(idCliente) del diagrama: el cliente se llega por el pedido. */
    List<FacturaCliente> findByPedido_PuntoVenta_Cliente_IdAndEliminadoFalseOrderByFechaFacturaDesc(String idCliente);
}
