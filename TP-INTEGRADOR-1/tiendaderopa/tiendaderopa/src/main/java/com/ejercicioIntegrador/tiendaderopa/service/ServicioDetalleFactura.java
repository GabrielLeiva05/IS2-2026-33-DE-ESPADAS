package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioDetalleFactura {

    @Autowired
    private RepositorioDetalleFactura repositorio; // Su propio repositorio

    @Autowired
    private ServicioProducto servicioProducto; // Comunicación Servicio a Servicio

    public DetalleFactura buscarPorId(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe el detalle de factura con id " + id));
    }

    public List<DetalleFactura> listarTodos() {
        return repositorio.findByEliminadoFalse();
    }

    @Transactional
    public DetalleFactura crearDetalleFactura(Factura factura) {
        DetalleFactura detalleFactura = new DetalleFactura();
        detalleFactura.setFactura(factura);
        detalleFactura.setEliminado(false);
        return repositorio.save(detalleFactura);
    }

    @Transactional
    public DetalleFactura modificarDetalleFactura(String idDetalleFactura, String codigoProducto) throws Exception {
        DetalleFactura detalle = buscarPorId(idDetalleFactura);
        Producto producto = servicioProducto.buscarProductoPorCodigo(codigoProducto); // Servicio a Servicio
        detalle.setProducto(producto);
        return repositorio.save(detalle);
    }

    @Transactional
    public void eliminarDetalleFactura(String idDetalleFactura) throws Exception {
        DetalleFactura detalle = buscarPorId(idDetalleFactura);
        detalle.setEliminado(true);
        repositorio.save(detalle);
    }

    public List<DetalleFactura> listarPorFactura(String facturaId) {
        return repositorio.findByFacturaIdAndEliminadoFalse(facturaId);
    }
}