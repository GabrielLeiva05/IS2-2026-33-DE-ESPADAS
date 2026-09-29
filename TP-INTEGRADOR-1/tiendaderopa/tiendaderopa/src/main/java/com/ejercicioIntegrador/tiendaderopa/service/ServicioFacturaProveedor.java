package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFormaDePago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Mismo patrón que ServicioFacturaCliente: por composición, no por
 * herencia. La firma de crearFactura es temporal (sin idProveedor
 * todavía) por la misma razón: Proveedor no existe aún en el proyecto.
 */
@Service
public class ServicioFacturaProveedor {

    @Autowired
    private RepositorioFacturaProveedor repositorio;
    @Autowired
    private ServicioFormaDePago svcFormaDePago;
    @Autowired
    private ServicioFactura svcFactura;
    @Autowired
    private ServicioProveedor svcProveedor;

    public void validar(Long numeroFactura, Date fechaFactura, String idFormaDePago) throws Exception {
        if (numeroFactura == null || numeroFactura < 1) {
            throw new Exception("El número de factura debe ser mayor a 0");
        }
        if (fechaFactura == null) {
            throw new Exception("La fecha de factura es obligatoria");
        }
        svcFormaDePago.buscarFormaDePago(idFormaDePago);
        // Pendiente: validar idProveedor cuando exista.
    }

    @Transactional
    public void crearFactura(Long numeroFactura, Date fechaFactura, double totalPago, String idFormaDePago) throws Exception {
        validar(numeroFactura, fechaFactura, idFormaDePago);

        FormaDePago formaDePago = svcFormaDePago.buscarFormaDePago(idFormaDePago);

        FacturaProveedor factura = new FacturaProveedor();
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setTotalPagado(totalPago);
        factura.setEstadoFactura(EstadoFactura.SIN_DEFINIR);
        factura.setFormaDePago(formaDePago);
        factura.setEliminado(false);

        repositorio.save(factura);
    }

    // --- A completar cuando exista Proveedor ---
    // public void modificarFactura(String id, ...) throws Exception { ... }

    public Collection<FacturaProveedor> listarActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public Collection<FacturaProveedor> listarPorEstado(EstadoFactura estado) {
        return repositorio.findByEstadoFactura(estado);
    }

// Pendiente: listarPorCliente(String idCliente)

    // ---------- Métodos usados por el ABM del dashboard ----------

    @Transactional(readOnly = true)
    public List<FacturaProveedor> listarTodas() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public FacturaProveedor buscarFactura(String id) throws Exception {
        return repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe la factura con id " + id));
    }

    @Transactional
    public void crearFactura(Long numeroFactura, Date fechaFactura, double totalPago, EstadoFactura estado,
                             String idFormaDePago, String idProveedor) throws Exception {
        validar(numeroFactura, fechaFactura, idFormaDePago);
        validarTotalYEstado(totalPago, estado);
        svcFactura.validarNumeroUnico(numeroFactura, null);
        FacturaProveedor factura = new FacturaProveedor();
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setTotalPagado(totalPago);
        factura.setEstadoFactura(estado);
        factura.setFormaDePago(svcFormaDePago.buscarFormaDePago(idFormaDePago));
        factura.setProveedor(svcProveedor.buscarPorId(idProveedor));
        factura.setEliminado(false);
        repositorio.save(factura);
    }

    @Transactional
    public void modificarFactura(String id, Long numeroFactura, Date fechaFactura, double totalPago,
                                 EstadoFactura estado, String idFormaDePago, String idProveedor) throws Exception {
        validar(numeroFactura, fechaFactura, idFormaDePago);
        validarTotalYEstado(totalPago, estado);
        svcFactura.validarNumeroUnico(numeroFactura, id);
        FacturaProveedor factura = buscarFactura(id);
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaFactura(fechaFactura);
        factura.setTotalPagado(totalPago);
        factura.setEstadoFactura(estado);
        factura.setFormaDePago(svcFormaDePago.buscarFormaDePago(idFormaDePago));
        factura.setProveedor(svcProveedor.buscarPorId(idProveedor));
        repositorio.save(factura);
    }

    private void validarTotalYEstado(double totalPago, EstadoFactura estado) throws Exception {
        if (totalPago < 0) {
            throw new Exception("El total pagado no puede ser negativo");
        }
        if (estado == null) {
            throw new Exception("El estado de la factura es obligatorio");
        }
    }
}
