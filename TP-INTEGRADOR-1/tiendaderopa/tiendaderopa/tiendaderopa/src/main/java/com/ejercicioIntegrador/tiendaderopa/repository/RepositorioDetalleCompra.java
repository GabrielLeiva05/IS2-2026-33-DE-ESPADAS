package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RepositorioDetalleCompra extends JpaRepository<DetalleCompra, String> {

    List<DetalleCompra> findByEliminadoFalse();

    List<DetalleCompra> findByOrdenCompraIdAndEliminadoFalse(String ordenCompraId);

    @Query("SELECT dc FROM DetalleCompra dc " +
            "JOIN FETCH dc.producto p " +
            "JOIN FETCH p.subCategoria sc " +
            "JOIN FETCH sc.categoria c " +
            "JOIN FETCH dc.ordenCompra oc " +
            "JOIN FETCH oc.facturaCliente fc " +
            "JOIN FETCH fc.formaDePago fp " +
            "WHERE dc.eliminado = false " +
            "AND oc.eliminado = false " +
            "AND fc.eliminado = false " +
            "AND fc.estadoFactura = com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura.PAGADA " +
            "AND oc.fecha BETWEEN :desde AND :hasta " +
            "ORDER BY oc.fecha ASC")
    List<DetalleCompra> buscarParaReporteVentas(@Param("desde") Date desde, @Param("hasta") Date hasta);
}