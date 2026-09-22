package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class ServicioFactura {

    @Autowired
    private RepositorioFactura repositorio;

    @Autowired
    private ServicioDetalleFactura svcDetalleFactura; // Servicio a Servicio

    @Transactional
    public void eliminarFactura(String id) throws Exception {
        Factura factura = buscarFactura(id);
        factura.setEliminado(true); // Baja lógica
        repositorio.save(factura);
    }

    public Collection<Factura> listarFactura() {
        return repositorio.findAll();
    }

    public Collection<Factura> listarFacturaActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public Collection<Factura> listarFacturaPorEstado(EstadoFactura estado) {
        return repositorio.findByEstadoFactura(estado);
    }

    public Factura buscarFactura(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe la factura con id " + id));
    }

    @Transactional
    public DetalleFactura crearDetalleFactura(String idFactura, String idDetalleCompra) throws Exception {
        Factura factura = buscarFactura(idFactura);
        DetalleFactura detalleFactura = svcDetalleFactura.crearDetalleFactura(factura); // Delega al Servicio
        repositorio.save(factura);
        return detalleFactura;
    }

    public DetalleFactura buscarDetalleFactura(String id) throws Exception {
        return svcDetalleFactura.buscarPorId(id); // Delega al Servicio
    }

    @Transactional
    public DetalleFactura modificarDetalleFactura(String idDetalleFactura, String codigoProducto) throws Exception {
        return svcDetalleFactura.modificarDetalleFactura(idDetalleFactura, codigoProducto); // Delega al Servicio
    }

    @Transactional
    public void eliminarDetalleFactura(String idDetalleFactura) throws Exception {
        svcDetalleFactura.eliminarDetalleFactura(idDetalleFactura); // Delega al Servicio
    }
}