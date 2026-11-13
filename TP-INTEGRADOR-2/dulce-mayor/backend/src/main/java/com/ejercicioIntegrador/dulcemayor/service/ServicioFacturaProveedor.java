package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FacturaProveedorRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RenglonFacturaDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.model.FacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.model.Proveedor;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioFacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.ConversorFactura;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.CreadorFactura;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.CreadorConcretoFacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.DatosFacturaProveedor;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.RenglonFactura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * FacturaProveedor (Ola 3): crearFacturaProveedor, modificarFacturaProveedor y listarFacturaPorProveedor del diagrama.
 * El alta y la modificación arman la factura con el creador del Factory Method (CreadorConcretoFacturaProveedor), igual que
 * la factura de cliente: mismos renglones, mismo IVA discriminado, mismo estado inicial.
 * Solo se modifica mientras está PENDIENTE_PAGO. No se da de baja por API.
 */
@Service
public class ServicioFacturaProveedor extends BaseService<FacturaProveedor, FacturaProveedorRequestDTO, FacturaResponseDTO> {

    private final RepositorioFacturaProveedor repositorioFacturaProveedor;
    private final CreadorFactura<DatosFacturaProveedor> creador;
    private final ServicioProveedor servicioProveedor;
    private final ServicioProducto servicioProducto;
    private final GeneradorPdfFactura generadorPdf;

    public ServicioFacturaProveedor(RepositorioFacturaProveedor repositorioFacturaProveedor,
                                    CreadorConcretoFacturaProveedor creador, ServicioProveedor servicioProveedor,
                                    ServicioProducto servicioProducto, GeneradorPdfFactura generadorPdf) {
        super(repositorioFacturaProveedor);
        this.repositorioFacturaProveedor = repositorioFacturaProveedor;
        this.creador = creador;
        this.servicioProveedor = servicioProveedor;
        this.servicioProducto = servicioProducto;
        this.generadorPdf = generadorPdf;
    }

    @Override
    protected FacturaProveedor crearEntidad(FacturaProveedorRequestDTO dto) throws MiException {
        // Factory Method: el creador concreto instancia la FacturaProveedor y emitir() la completa
        return (FacturaProveedor) creador.emitir(datos(dto));
    }

    /** modificarFacturaProveedor: se vuelve a armar con el creador y se reemplazan renglones e importes. */
    @Override
    protected void actualizarEntidad(FacturaProveedor factura, FacturaProveedorRequestDTO dto) throws MiException {
        if (factura.getEstado() != EstadoFactura.PENDIENTE_PAGO) {
            throw new MiException("Solo se modifica una factura pendiente de pago");
        }
        FacturaProveedor nueva = (FacturaProveedor) creador.emitir(datos(dto));
        factura.setProveedor(nueva.getProveedor());
        factura.setNumeroFactura(nueva.getNumeroFactura());
        factura.setFechaFactura(nueva.getFechaFactura());
        factura.getDetalles().clear();
        nueva.getDetalles().forEach(d -> factura.agregarDetalle(d.getProducto(), d.getCantidad(), d.getPrecioUnitario()));
        factura.setTotal(nueva.getTotal());
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, FacturaProveedor factura) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            throw new OperacionNoPermitidaException("Las facturas no se dan de baja");
        }
        if (factura.getFechaFactura().toLocalDate().isAfter(LocalDate.now())) {
            throw new MiException("La fecha de la factura no puede ser posterior a hoy");
        }
        Optional<FacturaProveedor> mismoNumero = repositorioFacturaProveedor
                .findFirstByProveedor_IdAndNumeroFacturaAndEliminadoFalse(factura.getProveedor().getId(), factura.getNumeroFactura());
        if (mismoNumero.isPresent() && !Objects.equals(mismoNumero.get().getId(), factura.getId())) {
            throw new MiException("Ya está cargada la factura N.º " + factura.getNumeroFactura() + " de ese proveedor");
        }
    }

    @Override
    public FacturaResponseDTO convertirAResponse(FacturaProveedor factura) {
        return ConversorFactura.aResponse(factura, creador.getAlicuotaIva());
    }

    @Override
    protected String nombreEntidad() {
        return "la factura de proveedor";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarActivos() {
        return convertirAResponse(repositorioFacturaProveedor.findByEliminadoFalseOrderByFechaFacturaDesc());
    }

    /** listarFacturaPorProveedor(idProveedor) del diagrama. */
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarFacturaPorProveedor(String idProveedor) throws MiException {
        servicioProveedor.obtenerEntidad(idProveedor);
        return convertirAResponse(repositorioFacturaProveedor.findByProveedor_IdAndEliminadoFalseOrderByFechaFacturaDesc(idProveedor));
    }

    /** generarPdf(id) del diagrama. */
    @Transactional(readOnly = true)
    public byte[] generarPdf(String idFactura) throws MiException {
        FacturaProveedor factura = obtenerEntidad(idFactura);
        return generadorPdf.generar(GeneradorPdfFactura.PLANTILLA_PROVEEDOR, factura.getId(), creador.getAlicuotaIva());
    }

    /** Resuelve proveedor y productos por sus servicios y arma los datos para el creador. */
    private DatosFacturaProveedor datos(FacturaProveedorRequestDTO dto) throws MiException {
        if (dto.numeroFactura() == null) {
            throw new MiException("Debe indicar el número de la factura del proveedor");
        }
        if (dto.fechaFactura() == null) {
            throw new MiException("Debe indicar la fecha de la factura");
        }
        if (dto.detalles() == null || dto.detalles().isEmpty()) {
            throw new MiException("La factura debe tener al menos un renglón");
        }
        Proveedor proveedor = obtenerReferencia(servicioProveedor, dto.idProveedor(), "el proveedor");
        List<RenglonFactura> renglones = new ArrayList<>();
        for (RenglonFacturaDTO renglon : dto.detalles()) {
            if (renglon == null || renglon.cantidad() == null || renglon.precioUnitario() == null) {
                throw new MiException("Cada renglón necesita producto, cantidad y precio unitario");
            }
            renglones.add(new RenglonFactura(obtenerReferencia(servicioProducto, renglon.idProducto(), "el producto"),
                    renglon.cantidad(), renglon.precioUnitario()));
        }
        return new DatosFacturaProveedor(proveedor, dto.numeroFactura(), dto.fechaFactura(), renglones);
    }
}
