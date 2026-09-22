package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFactura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class ServicioFactura {
    // Todo lo que el UML le puso a la clase base:
    // eliminarFactura, listarFactura, listarFacturaActivo, listarFacturaPorEstado, buscarFactura
    // + los métodos de DetalleFactura (crearDetalleFactura, buscarDetalleFactura, etc.)
    @Autowired
    private RepositorioFactura repositorio;

    @Transactional
    public void eliminarFactura(String id) throws Exception {
        Factura factura = buscarFactura(id);
        factura.setEliminado(true); // baja lógica, nunca deleteById
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

    // --- A completar cuando exista DetalleFactura ---
    // public DetalleFactura crearDetalleFactura(String idFactura, String idProducto, int cantidad) throws Exception { ... }
    // public DetalleFactura buscarDetalleFactura(String id) throws Exception { ... }
    // public DetalleFactura modificarDetalleFactura(String idDetalleFactura, String idProducto) throws Exception { ... }
    // public void eliminarDetalleFactura(String idDetalleFactura) throws Exception { ... }
}
