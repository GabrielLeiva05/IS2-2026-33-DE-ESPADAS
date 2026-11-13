package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FacturaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SolicitudFacturaClienteDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoFactura;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioFacturaCliente;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.ConversorFactura;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.CreadorFactura;
import com.ejercicioIntegrador.dulcemayor.service.facturacion.CreadorConcretoFacturaCliente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FacturaCliente (Ola 3, Facturación sin Pagos).
 *
 * Cuándo se factura: al CONFIRMAR el cliente su pedido (decisión del equipo, Ola 2: "confirmado = se crea la
 * FacturaCliente en PENDIENTE_PAGO"). ServicioPedido llama a facturarPedido() dentro de la misma transacción en que
 * registra fechaFacturacion y pasa el pedido a PENDIENTE_DE_ENTREGA: si la factura falla, la confirmación se deshace.
 *
 * Algoritmo de alta (Template Method de BaseService): crearEntidad delega en el creador del Factory Method
 * (CreadorConcretoFacturaCliente); validar impide facturar dos veces el mismo pedido; preAlta asigna el número correlativo.
 * Las facturas no se modifican ni se dan de baja por API (no hay anulación de pedidos confirmados).
 */
@Service
public class ServicioFacturaCliente extends BaseService<FacturaCliente, SolicitudFacturaClienteDTO, FacturaResponseDTO> {

    private final RepositorioFacturaCliente repositorioFacturaCliente;
    private final CreadorFactura<Pedido> creador;
    private final ServicioCliente servicioCliente;
    private final GeneradorPdfFactura generadorPdf;

    public ServicioFacturaCliente(RepositorioFacturaCliente repositorioFacturaCliente,
                                  CreadorConcretoFacturaCliente creador, ServicioCliente servicioCliente,
                                  GeneradorPdfFactura generadorPdf) {
        super(repositorioFacturaCliente);
        this.repositorioFacturaCliente = repositorioFacturaCliente;
        this.creador = creador;
        this.servicioCliente = servicioCliente;
        this.generadorPdf = generadorPdf;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected FacturaCliente crearEntidad(SolicitudFacturaClienteDTO solicitud) throws MiException {
        if (solicitud.pedido() == null) {
            throw new MiException("Debe indicar el pedido a facturar");
        }
        // Factory Method: el creador concreto instancia la FacturaCliente y emitir() la completa
        return (FacturaCliente) creador.emitir(solicitud.pedido());
    }

    @Override
    protected void actualizarEntidad(FacturaCliente factura, SolicitudFacturaClienteDTO dto) throws MiException {
        throw new OperacionNoPermitidaException("Las facturas de cliente no se modifican");
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, FacturaCliente factura) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            throw new OperacionNoPermitidaException("Las facturas no se dan de baja");
        }
        Pedido pedido = factura.getPedido();
        if (pedido.getFechaFacturacion() == null) {
            throw new MiException("Solo se factura un pedido confirmado por el cliente");
        }
        if (pedido.getEstado() == EstadoPedido.ANULADO) {
            throw new MiException("No se puede facturar un pedido anulado");
        }
        if (casoDeUso == BaseUseCaseService.ALTA && repositorioFacturaCliente.existsByPedido_Id(pedido.getId())) {
            throw new MiException("El pedido N.º " + pedido.getNumeroPedido() + " ya tiene factura");
        }
    }

    /** Numeración correlativa propia de las facturas de cliente (1, 2, 3…). La columna pedido_id es única. */
    @Override
    protected void preAlta(FacturaCliente factura) {
        long ultimo = repositorioFacturaCliente.findTopByOrderByNumeroFacturaDesc()
                .map(FacturaCliente::getNumeroFactura).orElse(0L);
        factura.setNumeroFactura(ultimo + 1);
    }

    @Override
    public FacturaResponseDTO convertirAResponse(FacturaCliente factura) {
        return ConversorFactura.aResponse(factura, creador.getAlicuotaIva());
    }

    @Override
    protected String nombreEntidad() {
        return "la factura";
    }

    // ------------------------------------------------------------------
    // Operaciones propias (diagrama de clases)
    // ------------------------------------------------------------------

    /** crearFacturaCliente(idPedido) del diagrama. La llama ServicioPedido al confirmar, en su transacción. */
    @Transactional(rollbackFor = Exception.class)
    public FacturaCliente facturarPedido(Pedido pedido) throws MiException {
        return altaEntidad(new SolicitudFacturaClienteDTO(pedido));
    }

    /** Facturas de clientes activas (administración), de la más nueva a la más vieja. */
    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarActivos() {
        return convertirAResponse(repositorioFacturaCliente.findByEliminadoFalseOrderByNumeroFacturaDesc());
    }

    /** listarFacturaPorCliente(idCliente) del diagrama, para el cliente autenticado (se identifica por su token). */
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarFacturasDelCliente(String correo) throws MiException {
        Cliente cliente = servicioCliente.obtenerClientePorCorreo(correo);
        return convertirAResponse(repositorioFacturaCliente
                .findByPedido_PuntoVenta_Cliente_IdAndEliminadoFalseOrderByFechaFacturaDesc(cliente.getId()));
    }

    /** Factura del cliente autenticado. Si no es suya responde 404, igual que si no existiera. */
    @Transactional(readOnly = true)
    public FacturaResponseDTO consultarFacturaDelCliente(String correo, String idFactura) throws MiException {
        return convertirAResponse(facturaDelCliente(correo, idFactura));
    }

    /** generarPdf(id) del diagrama (administración). */
    @Transactional(readOnly = true)
    public byte[] generarPdf(String idFactura) throws MiException {
        FacturaCliente factura = obtenerEntidad(idFactura);
        return generadorPdf.generar(GeneradorPdfFactura.PLANTILLA_CLIENTE, factura.getId(), creador.getAlicuotaIva());
    }

    /** PDF de una factura del cliente autenticado (solo las suyas). */
    @Transactional(readOnly = true)
    public byte[] generarPdfDelCliente(String correo, String idFactura) throws MiException {
        FacturaCliente factura = facturaDelCliente(correo, idFactura);
        return generadorPdf.generar(GeneradorPdfFactura.PLANTILLA_CLIENTE, factura.getId(), creador.getAlicuotaIva());
    }

    /** Factura de cada pedido (para mostrar el total con IVA y el enlace a la factura junto al pedido). */
    @Transactional(readOnly = true)
    public Map<String, FacturaCliente> facturasDePedidos(Collection<String> idsPedido) {
        if (idsPedido == null || idsPedido.isEmpty()) {
            return Map.of();
        }
        return repositorioFacturaCliente.findByPedido_IdInAndEliminadoFalse(idsPedido).stream()
                .collect(Collectors.toMap(f -> f.getPedido().getId(), Function.identity(), (a, b) -> a));
    }

    @Transactional(readOnly = true)
    public Optional<FacturaCliente> facturaDelPedido(String idPedido) {
        return repositorioFacturaCliente.findFirstByPedido_IdAndEliminadoFalse(idPedido);
    }

    /**
     * Marca como PAGADA la factura del pedido cuyo pago se acreditó (lo llama ServicioPago al aprobarse un QR).
     * Es idempotente: si ya estaba pagada no vuelve a escribir.
     */
    @Transactional(rollbackFor = Exception.class)
    public Optional<FacturaCliente> marcarPagada(String idPedido) {
        Optional<FacturaCliente> factura = repositorioFacturaCliente.findFirstByPedido_IdAndEliminadoFalse(idPedido);
        factura.ifPresent(f -> {
            if (f.getEstado() != EstadoFactura.PAGADA) {
                f.setEstado(EstadoFactura.PAGADA);
                repositorioFacturaCliente.save(f);
            }
        });
        return factura;
    }

    private FacturaCliente facturaDelCliente(String correo, String idFactura) throws MiException {
        Cliente cliente = servicioCliente.obtenerClientePorCorreo(correo);
        FacturaCliente factura = repositorioFacturaCliente.findByIdAndEliminadoFalse(idFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura indicada"));
        if (!factura.getPedido().getPuntoVenta().getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNoEncontradoException("No existe la factura indicada");
        }
        return factura;
    }
}
