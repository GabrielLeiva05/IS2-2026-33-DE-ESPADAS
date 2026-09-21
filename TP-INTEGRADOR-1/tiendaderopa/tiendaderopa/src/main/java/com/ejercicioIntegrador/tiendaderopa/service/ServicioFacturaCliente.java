package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.Factura;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFacturaCliente;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFormaDePago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class ServicioFacturaCliente {

        @Autowired
        private ServicioFormaDePago svcFormaDePago;
        @Autowired
        private RepositorioFacturaCliente repositorio;

        public void crearFactura(Long numeroFactura, Date fechaFactura, double totalPago, EstadoFactura estado, String idFormaDePago) throws Exception {
            validar(numeroFactura, fechaFactura, totalPago, estado);
            //validarNumeroFacturaUnico(numeroFactura, null);
            //Socio socio = socioService.buscarSocio(idSocio);
            FormaDePago formaDePago = svcFormaDePago.buscarFormaDePago(idFormaDePago);

            FacturaCliente factura = new FacturaCliente();
            factura.setNumeroFactura(numeroFactura);
            factura.setFechaFactura(fechaFactura);
            factura.setEstadoFactura(estado);
            factura.setFormaDePago(formaDePago);
            factura.setTotalPagado(totalPago);

            repositorio.save(factura);
            //List<DetalleFactura> detalles = new ArrayList<>();
            //facturaPersistida.setDetalles(detalles);
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
        // --- A completar cuando exista Cliente/Empleado/OrdenCompra ---
        // public void modificarFactura(String id, ...) throws Exception { ... }

        public Collection<FacturaCliente> listarActivo() {
            return repositorio.findByEliminadoFalse();
        }

        public Collection<FacturaCliente> listarPorEstado(EstadoFactura estado) {
            return repositorio.findByEstadoFactura(estado);
        }

    // Pendiente: listarPorCliente(String idCliente)
    }
