package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.FacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioFacturaProveedor extends BaseRepository<FacturaProveedor> {

    Optional<FacturaProveedor> findFirstByProveedor_IdAndNumeroFacturaAndEliminadoFalse(String idProveedor, long numeroFactura);

    List<FacturaProveedor> findByEliminadoFalseOrderByFechaFacturaDesc();

    /** listarFacturaPorProveedor(idProveedor) del diagrama. */
    List<FacturaProveedor> findByProveedor_IdAndEliminadoFalseOrderByFechaFacturaDesc(String idProveedor);
}
