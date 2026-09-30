package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFacturaCliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

@Service
public class ServicioFacturaCliente {

    @Autowired
    private RepositorioFacturaCliente repositorio;

    @Autowired
    private ServicioFormaDePago svcFormaDePago; // Servicio a Servicio

    public void crearFactura(Long numeroFactura, Date fechaFactura, double totalPago, EstadoFactura estado, String idFormaDePago) throws Exception {
        validar(numeroFactura, fechaFactura, totalPago, estado);
        FormaDePago formaDePago = svcFormaDePago.buscarFormaDePago(idFormaDePago);

        FacturaCliente factura = new FacturaCliente();
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setEstadoFactura(estado);
        factura.setFormaDePago(formaDePago);
        factura.setTotalPagado(totalPago);

        repositorio.save(factura);
    }

    public void validar(Long numeroFactura, Date fechaFactura, double totalPago, EstadoFactura estado) throws Exception {
        if (numeroFactura == null) {
            throw new Exception("El numero de factura es obligatorio");
        }
        if (fechaFactura == null) {
            throw new Exception("La fecha de la factura es obligatoria");
        }
        if (totalPago < 0) {
            throw new Exception("El total pagado no puede ser negativo");
        }
        if (estado == null) {
            throw new Exception("El estado de la factura es obligatorio");
        }
    }

    public Collection<FacturaCliente> listarActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public Collection<FacturaCliente> listarTodas() {
        return repositorio.findAll();
    }

    public Collection<FacturaCliente> listarPorEstado(EstadoFactura estado) {
        return repositorio.findByEstadoFactura(estado);
    }

    public FacturaCliente buscarPorId(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe una factura de cliente con id " + id));
    }

    public void modificarFactura(String id, Long numeroFactura, Date fechaFactura, double totalPago,
            EstadoFactura estado, String idFormaDePago) throws Exception {
        validar(numeroFactura, fechaFactura, totalPago, estado);
        FormaDePago formaDePago = svcFormaDePago.buscarFormaDePago(idFormaDePago);

        FacturaCliente factura = buscarPorId(id);
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setEstadoFactura(estado);
        factura.setFormaDePago(formaDePago);
        factura.setTotalPagado(totalPago);
        repositorio.save(factura);
    }

    public void eliminarFactura(String id) throws Exception {
        FacturaCliente factura = buscarPorId(id);
        factura.setEliminado(true);
        repositorio.save(factura);
    }
}