package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.*;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFacturaProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;

@Service
public class ServicioFacturaProveedor {

    @Autowired
    private RepositorioFacturaProveedor repositorio;

    @Autowired
    private ServicioFormaDePago svcFormaDePago; // Servicio a Servicio

    @Autowired
    private ServicioProveedor svcProveedor;
    @Autowired
    private ServicioOrdenCompraProveedor svcOrdenCompraProveedor;

    public void validar(Long numeroFactura, Date fechaFactura, String idFormaDePago) throws Exception {
        if (numeroFactura == null || numeroFactura < 1) {
            throw new Exception("El número de factura debe ser mayor a 0");
        }
        if (fechaFactura == null) {
            throw new Exception("La fecha de factura es obligatoria");
        }
        svcFormaDePago.buscarFormaDePago(idFormaDePago);
    }

    @Transactional
    public FacturaProveedor crearFactura(Long numeroFactura, Date fechaFactura, double totalPago,
                                         String idFormaDePago, String idProveedor,
                                         String idOrdenCompraProveedor) throws Exception {
        validar(numeroFactura, fechaFactura, idFormaDePago);

        FormaDePago formaDePago = svcFormaDePago.buscarFormaDePago(idFormaDePago);
        Proveedor proveedor = svcProveedor.buscarProveedor(idProveedor);
        OrdenCompraProveedor orden = svcOrdenCompraProveedor.buscarOrdenCompraProveedor(idOrdenCompraProveedor);

        FacturaProveedor factura = new FacturaProveedor();
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setTotalPagado(totalPago);
        factura.setEstadoFactura(EstadoFactura.SIN_DEFINIR);
        factura.setFormaDePago(formaDePago);
        factura.setProveedor(proveedor);
        factura.setOrdenCompraProveedor(orden);
        factura.setEliminado(false);

        return repositorio.save(factura);
    }

    public Collection<FacturaProveedor> listarActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public Collection<FacturaProveedor> listarPorEstado(EstadoFactura estado) {
        return repositorio.findByEstadoFactura(estado);
    }

    public FacturaProveedor buscarPorOrdenCompra(String idOrdenCompraProveedor) throws Exception {
        return repositorio.findByOrdenCompraProveedor_Id(idOrdenCompraProveedor)
                .orElseThrow(() -> new Exception("No existe una factura asociada a esa orden de compra"));
    }

    public FacturaProveedor buscarPorNumeroFactura(Long numeroFactura) {
        return repositorio.findByNumeroFactura(numeroFactura);
    }
}