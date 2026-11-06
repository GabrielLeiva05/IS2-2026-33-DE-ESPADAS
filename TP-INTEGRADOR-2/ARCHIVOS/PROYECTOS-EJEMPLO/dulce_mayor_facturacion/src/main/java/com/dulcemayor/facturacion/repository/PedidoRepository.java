package com.dulcemayor.facturacion.repository;

import com.dulcemayor.facturacion.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumeroFactura(String numeroFactura);
}
