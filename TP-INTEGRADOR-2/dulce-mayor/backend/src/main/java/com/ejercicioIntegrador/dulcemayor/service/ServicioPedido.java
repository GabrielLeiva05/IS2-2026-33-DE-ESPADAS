package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.AsignacionPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DetallePedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LineaPedidoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoAltaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResumenDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaDelDiaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UbicacionDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ResultadoVisita;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.DetallePedido;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.PuntoRuta;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Ruta;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Pedidos (HU-21) y su ciclo de vida:
 *
 *   CARGADO ──(cliente confirma)──> PENDIENTE_DE_ENTREGA ──(distribuidor entrega)──> ENTREGADO
 *      └────(cliente anula)──> ANULADO (el stock vuelve al almacén, una sola vez)
 *
 * - Stock: se descuenta UNA vez, al cargar el pedido (reserva), a través de ServicioStock con bloqueo de fila.
 *   Al anular se devuelve. Confirmar no vuelve a descontar.
 * - Carga del pedido y visita: el pedido, el descuento de stock y la marca "visita con pedido" se hacen en
 *   la misma transacción. Si algo falla (por ejemplo, falta stock) no queda ninguna de las tres cosas.
 * - Confirmación: no hay módulo de pagos todavía. Confirmar registra la fecha (fechaFacturacion del diagrama)
 *   y deja el pedido PENDIENTE_DE_ENTREGA, que es la regla del diagrama para el pago en efectivo al entregar.
 * - Entrega fallida: la parada queda NO_ENTREGADO con su motivo; el pedido NO cambia (sigue pendiente) y entra
 *   en la ruta de entregas del día siguiente.
 * - Propiedad: el cliente, el preventista y el distribuidor se identifican por el token; un pedido ajeno
 *   responde 404 (no se revela si existe).
 */
@Service
public class ServicioPedido extends BaseService<Pedido, PedidoAltaDTO, PedidoResponseDTO> {

    private final RepositorioPedido repositorioPedido;
    private final ServicioFacturaCliente servicioFacturaCliente;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioCliente servicioCliente;
    private final ServicioPuntoVenta servicioPuntoVenta;
    private final ServicioProducto servicioProducto;
    private final ServicioCatalogo servicioCatalogo;
    private final ServicioStock servicioStock;
    private final ServicioRuta servicioRuta;
    private final ServicioPago servicioPago;
    private final ServicioCorreo servicioCorreo;

    public ServicioPedido(RepositorioPedido repositorioPedido, ServicioEmpleado servicioEmpleado,
                          ServicioCliente servicioCliente, ServicioPuntoVenta servicioPuntoVenta,
                          ServicioProducto servicioProducto, ServicioCatalogo servicioCatalogo,
                          ServicioStock servicioStock, ServicioRuta servicioRuta,
                          ServicioFacturaCliente servicioFacturaCliente, ServicioPago servicioPago,
                          ServicioCorreo servicioCorreo) {
        super(repositorioPedido);
        this.repositorioPedido = repositorioPedido;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioCliente = servicioCliente;
        this.servicioPuntoVenta = servicioPuntoVenta;
        this.servicioProducto = servicioProducto;
        this.servicioCatalogo = servicioCatalogo;
        this.servicioStock = servicioStock;
        this.servicioRuta = servicioRuta;
        this.servicioFacturaCliente = servicioFacturaCliente;
        this.servicioPago = servicioPago;
        this.servicioCorreo = servicioCorreo;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method (alta = carga del pedido por el preventista)
    // ------------------------------------------------------------------

    @Override
    protected Pedido crearEntidad(PedidoAltaDTO dto) throws MiException {
        Empleado empleado = obtenerReferencia(servicioEmpleado, dto.idPreventista(), "el preventista");
        if (!(empleado instanceof Preventista preventista)) {
            throw new MiException("Solo un preventista puede cargar pedidos");
        }
        Pedido pedido = new Pedido();
        pedido.setPreventista(preventista);
        pedido.setPuntoVenta(obtenerReferencia(servicioPuntoVenta, dto.idPuntoVenta(), "el punto de venta"));
        pedido.setEstado(EstadoPedido.CARGADO);
        pedido.setFechaHoraCarga(LocalDateTime.now());
        for (Map.Entry<String, Integer> linea : agruparLineas(dto.detalles()).entrySet()) {
            pedido.agregarDetalle(crearDetalle(linea.getKey(), linea.getValue()));
        }
        // calcularTotal del diagrama: el descuento queda en 0 hasta implementar promociones (HU-22).
        double subtotal = pedido.getDetalles().stream().mapToDouble(DetallePedido::getSubtotal).sum();
        pedido.setSubtotal(redondear(subtotal));
        pedido.setDescuento(0);
        pedido.setTotal(redondear(subtotal));
        return pedido;
    }

    @Override
    protected void actualizarEntidad(Pedido pedido, PedidoAltaDTO dto) throws MiException {
        throw new OperacionNoPermitidaException(
                "Un pedido cargado no se modifica: el cliente puede anularlo y el preventista cargar uno nuevo");
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Pedido pedido) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            throw new OperacionNoPermitidaException("Los pedidos no se dan de baja: se anulan");
        }
        if (pedido.getDetalles().isEmpty()) {
            throw new MiException("El pedido debe tener al menos un producto");
        }
        if (pedido.getPuntoVenta().isEliminado()) {
            throw new MiException("El punto de venta está dado de baja");
        }
        if (pedido.getPuntoVenta().getCliente().isEliminado()) {
            throw new MiException("El cliente del punto de venta está dado de baja");
        }
        for (DetallePedido detalle : pedido.getDetalles()) {
            int disponible = servicioStock.cantidadDisponible(detalle.getProducto().getId());
            if (detalle.getCantidad() > disponible) {
                throw new MiException("No hay stock suficiente de " + detalle.getProducto().getNombre()
                        + ": disponible " + disponible + ", pedido " + detalle.getCantidad());
            }
        }
    }

    /** Reserva: descuenta el stock de cada renglón (con bloqueo de fila) y numera el pedido. */
    @Override
    protected void preAlta(Pedido pedido) throws MiException {
        for (DetallePedido detalle : pedido.getDetalles()) {
            servicioStock.descontarStock(detalle.getProducto().getId(), detalle.getCantidad());
        }
        long ultimo = repositorioPedido.findTopByOrderByNumeroPedidoDesc().map(Pedido::getNumeroPedido).orElse(1000L);
        pedido.setNumeroPedido(ultimo + 1);
    }

    /** La visita del punto en la ruta de hoy queda "con pedido", dentro de la misma transacción. */
    @Override
    protected void postAlta(Pedido pedido) throws MiException {
        Ruta rutaDeHoy = servicioRuta.buscarRutaDelDia(pedido.getPreventista(), TipoRuta.VISITA, LocalDate.now())
                .orElse(null);
        if (rutaDeHoy == null) {
            return;
        }
        PuntoRuta parada = servicioRuta.buscarParada(rutaDeHoy, pedido.getPuntoVenta().getId()).orElse(null);
        if (parada != null && parada.getResultado() != ResultadoVisita.CON_PEDIDO) {
            servicioRuta.registrarResultado(rutaDeHoy, pedido.getPuntoVenta().getId(), ResultadoVisita.CON_PEDIDO, null);
        }
    }

    @Override
    public PedidoResponseDTO convertirAResponse(Pedido pedido) {
        return convertir(pedido, servicioFacturaCliente.facturaDelPedido(pedido.getId()).orElse(null),
                servicioPago.pagoDelPedido(pedido.getId()).orElse(null));
    }

    /** Listados: las facturas y los pagos de todos los pedidos se buscan en una sola consulta cada uno. */
    @Override
    protected List<PedidoResponseDTO> convertirAResponse(java.util.Collection<Pedido> pedidos) {
        Map<String, FacturaCliente> facturas = servicioFacturaCliente.facturasDePedidos(
                pedidos.stream().map(Pedido::getId).toList());
        Map<String, Pago> pagos = servicioPago.pagosDePedidos(pedidos.stream().map(Pedido::getId).toList());
        return pedidos.stream().map(p -> convertir(p, facturas.get(p.getId()), pagos.get(p.getId()))).toList();
    }

    private PedidoResponseDTO convertir(Pedido pedido, FacturaCliente factura, Pago pago) {
        PuntoVenta pv = pedido.getPuntoVenta();
        Cliente cliente = pv.getCliente();
        Preventista preventista = pedido.getPreventista();
        Distribuidor distribuidor = pedido.getDistribuidor();
        List<DetallePedidoResponseDTO> detalles = pedido.getDetalles().stream()
                .map(d -> new DetallePedidoResponseDTO(d.getId(), d.getProducto().getId(), d.getProducto().getCodigo(),
                        d.getProducto().getNombre(), d.getProducto().getPresentacion(),
                        d.getProducto().getUnidadesPorPresentacion(), d.getCantidad(), d.getPrecioUnitario(),
                        d.getSubtotal(),
                        d.getProducto().getImagenes().isEmpty() ? null : d.getProducto().getImagenes().get(0).getId()))
                .toList();
        boolean cargado = pedido.getEstado() == EstadoPedido.CARGADO;
        return new PedidoResponseDTO(pedido.getId(), pedido.getNumeroPedido(), pedido.getFechaHoraCarga(),
                pedido.getFechaFacturacion(), pedido.getFechaDespacho(), pedido.getFechaEntrega(),
                pedido.getSubtotal(), pedido.getDescuento(), pedido.getTotal(), pedido.getEstado(),
                pedido.isConfirmado(), cargado && !pedido.isConfirmado(), cargado, pv.getId(),
                pv.getNombreFantasia(), ServicioPuntoVenta.textoDireccion(pv), cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(), preventista.getId(),
                preventista.getNombre() + " " + preventista.getApellido(),
                distribuidor == null ? null : distribuidor.getId(),
                distribuidor == null ? null : distribuidor.getNombre() + " " + distribuidor.getApellido(),
                detalles, pedido.isEliminado(),
                factura == null ? null : factura.getId(), factura == null ? null : factura.getNumeroFactura(),
                factura == null ? null : factura.getTotal(),
                pedido.getFormaDePago(), pago == null ? null : pago.getEstado());
    }

    @Override
    protected String nombreEntidad() {
        return "el pedido";
    }

    // ------------------------------------------------------------------
    // Preventista
    // ------------------------------------------------------------------

    /** crearPedido del diagrama: el preventista es el autenticado, nunca uno enviado en el cuerpo. */
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO cargarPedido(String correoPreventista, PedidoRequestDTO pedido) throws MiException {
        if (pedido == null) {
            throw new MiException("Debe indicar el pedido");
        }
        Empleado preventista = servicioEmpleado.obtenerEmpleadoPorCorreo(correoPreventista);
        return convertirAResponse(altaEntidad(new PedidoAltaDTO(preventista.getId(), pedido.idPuntoVenta(),
                pedido.detalles())));
    }

    /** listarPedidoPorPreventista del diagrama, para "Mis pedidos" de la app. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosDelPreventista(String correoPreventista) throws MiException {
        Empleado preventista = servicioEmpleado.obtenerEmpleadoPorCorreo(correoPreventista);
        return convertirAResponse(repositorioPedido.findByPreventista_IdAndEliminadoFalseOrderByFechaHoraCargaDesc(
                preventista.getId()));
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO consultarPedidoDelPreventista(String correoPreventista, String id) throws MiException {
        Empleado preventista = servicioEmpleado.obtenerEmpleadoPorCorreo(correoPreventista);
        Pedido pedido = obtenerEntidad(id);
        if (!pedido.getPreventista().getId().equals(preventista.getId())) {
            throw new RecursoNoEncontradoException("No existe el pedido con id " + id);
        }
        return convertirAResponse(pedido);
    }

    /**
     * Confirmación en forma presencial por el preventista, con el medio de pago preferido por el cliente.
     * En UNA transacción (con el pedido bloqueado):
     *  1. registra fechaFacturacion, el medio de pago y pasa el pedido a PENDIENTE_DE_ENTREGA;
     *  2. emite la FacturaCliente en PENDIENTE_PAGO;
     *  3. registra el Pago y envía al cliente el correo con el detalle y las instrucciones (QR, alias o efectivo).
     * Si el pago con QR no se puede generar en Mercado Pago, se deshace todo y el pedido sigue CARGADO.
     */
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO confirmarPedidoPresencial(String correoPreventista, String id, FormaDePago formaDePago)
            throws MiException {
        if (formaDePago == null) {
            throw new MiException("Debe indicar el medio de pago preferido del cliente");
        }
        Empleado empleado = servicioEmpleado.obtenerEmpleadoPorCorreo(correoPreventista);
        if (!(empleado instanceof Preventista preventista)) {
            throw new MiException("La cuenta no pertenece a un preventista");
        }
        Pedido pedido = bloquear(id);
        if (!pedido.getPreventista().getId().equals(preventista.getId())) {
            throw new RecursoNoEncontradoException("No existe el pedido con id " + id);
        }
        if (pedido.getEstado() != EstadoPedido.CARGADO || pedido.isConfirmado()) {
            throw new MiException("El pedido N.º " + pedido.getNumeroPedido() + " ya no está pendiente de confirmación");
        }
        pedido.setFechaFacturacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE_DE_ENTREGA);
        pedido.setFormaDePago(formaDePago);
        Pedido guardado = repositorioPedido.save(pedido);
        FacturaCliente factura = servicioFacturaCliente.facturarPedido(guardado);
        Pago pago = servicioPago.registrarPagoDePedido(guardado, factura, formaDePago);
        return convertir(guardado, factura, pago);
    }

    // ------------------------------------------------------------------
    // Cliente
    // ------------------------------------------------------------------
    /** listarPedidoPorCliente del diagrama, para el cliente autenticado. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosDelCliente(String correoCliente) throws MiException {
        Cliente cliente = servicioCliente.obtenerClientePorCorreo(correoCliente);
        return convertirAResponse(repositorioPedido.findByPuntoVenta_Cliente_IdAndEliminadoFalseOrderByFechaHoraCargaDesc(
                cliente.getId()));
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO consultarPedidoDelCliente(String correoCliente, String id) throws MiException {
        return convertirAResponse(pedidoDelCliente(correoCliente, obtenerEntidad(id)));
    }

    /**
     * El cliente confirma un pedido CARGADO. En UNA transacción (con el pedido bloqueado):
     *  1. se registra fechaFacturacion y el pedido pasa a PENDIENTE_DE_ENTREGA (sin Pagos se cobra al entregar);
     *  2. se emite la FacturaCliente en PENDIENTE_PAGO (Ola 3), con los mismos renglones, precios y descuento.
     * Si la factura no se puede emitir, se deshace todo y el pedido sigue CARGADO. Una segunda confirmación se
     * rechaza (el pedido ya no está CARGADO) y la columna única pedido_id impide una factura duplicada.
     */
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO confirmarPedido(String correoCliente, String id) throws MiException {
        Pedido pedido = pedidoDelCliente(correoCliente, bloquear(id));
        if (pedido.getEstado() != EstadoPedido.CARGADO || pedido.isConfirmado()) {
            throw new MiException("El pedido N.º " + pedido.getNumeroPedido() + " ya no está pendiente de confirmación");
        }
        pedido.setFechaFacturacion(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE_DE_ENTREGA);
        Pedido guardado = repositorioPedido.save(pedido);
        FacturaCliente factura = servicioFacturaCliente.facturarPedido(guardado);
        return convertir(guardado, factura, null);
    }

    /**
     * anularPedido del diagrama, por el cliente: solo mientras está CARGADO. Devuelve al stock lo reservado.
     * El bloqueo del pedido y el cambio de estado garantizan que la devolución ocurra una sola vez.
     */
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO anularPedido(String correoCliente, String id) throws MiException {
        Pedido pedido = pedidoDelCliente(correoCliente, bloquear(id));
        if (pedido.getEstado() != EstadoPedido.CARGADO) {
            throw new MiException("El pedido N.º " + pedido.getNumeroPedido() + " ya no se puede anular");
        }
        pedido.setEstado(EstadoPedido.ANULADO);
        for (DetallePedido detalle : pedido.getDetalles()) {
            servicioStock.reponerStock(detalle.getProducto().getId(), detalle.getCantidad());
        }
        return convertirAResponse(repositorioPedido.save(pedido));
    }

    // ------------------------------------------------------------------
    // Administración: asignación a distribuidores
    // ------------------------------------------------------------------

    /** listarPedidoPorEstado del diagrama. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidoPorEstado(EstadoPedido estado) throws MiException {
        if (estado == null) {
            throw new MiException("Debe indicar el estado");
        }
        return convertirAResponse(repositorioPedido.findByEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(estado));
    }

    /**
     * asignarDistribuidor del diagrama: solo pedidos PENDIENTE_DE_ENTREGA. Registra la fecha de despacho.
     * El distribuidor los verá en su ruta de entregas al generarla.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PedidoResponseDTO> asignarDistribuidor(AsignacionPedidosDTO asignacion) throws MiException {
        if (asignacion == null || asignacion.idsPedido() == null || asignacion.idsPedido().isEmpty()) {
            throw new MiException("Elegí al menos un pedido para asignar");
        }
        Empleado empleado = obtenerReferencia(servicioEmpleado, asignacion.idDistribuidor(), "el distribuidor");
        if (!(empleado instanceof Distribuidor distribuidor)) {
            throw new MiException("Los pedidos se asignan a un distribuidor");
        }
        List<PedidoResponseDTO> asignados = new ArrayList<>();
        for (String id : asignacion.idsPedido().stream().distinct().toList()) {
            Pedido pedido = bloquear(id);
            if (pedido.getEstado() != EstadoPedido.PENDIENTE_DE_ENTREGA) {
                throw new MiException("El pedido N.º " + pedido.getNumeroPedido()
                        + " no está pendiente de entrega (estado " + pedido.getEstado() + ")");
            }
            pedido.setDistribuidor(distribuidor);
            pedido.setFechaDespacho(LocalDateTime.now());
            asignados.add(convertirAResponse(repositorioPedido.save(pedido)));
        }
        return asignados;
    }

    // ------------------------------------------------------------------
    // Reportes administrativos (los consume ServicioReporte; este servicio es el único dueño de RepositorioPedido)
    // ------------------------------------------------------------------

    /** Pedidos activos cargados entre las dos fechas (hastaExclusivo no se incluye), del más antiguo al más nuevo. */
    @Transactional(readOnly = true)
    public List<Pedido> listarEntidadesCargadasEntre(LocalDateTime desde, LocalDateTime hastaExclusivo) {
        return repositorioPedido
                .findByEliminadoFalseAndFechaHoraCargaGreaterThanEqualAndFechaHoraCargaLessThanOrderByFechaHoraCargaAsc(
                        desde, hastaExclusivo);
    }

    /** Pedidos activos confirmados (con fecha de facturación) entre las dos fechas (hastaExclusivo no se incluye). */
    @Transactional(readOnly = true)
    public List<Pedido> listarEntidadesFacturadasEntre(LocalDateTime desde, LocalDateTime hastaExclusivo) {
        return repositorioPedido
                .findByEliminadoFalseAndFechaFacturacionGreaterThanEqualAndFechaFacturacionLessThanOrderByFechaFacturacionAsc(
                        desde, hastaExclusivo);
    }

    // ------------------------------------------------------------------
    // Distribuidor
    // ------------------------------------------------------------------

    /** listarPedidoPorDistribuidor: sus pedidos pendientes, incluidos los reprogramados de días anteriores. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPendientesDelDistribuidor(String correoDistribuidor) throws MiException {
        Distribuidor distribuidor = distribuidor(correoDistribuidor);
        return convertirAResponse(repositorioPedido.findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(
                distribuidor.getId(), EstadoPedido.PENDIENTE_DE_ENTREGA));
    }

    /**
     * Genera (o actualiza) la ruta de entregas de hoy con los comercios de sus pedidos pendientes y la ordena
     * desde la ubicación indicada. Los comercios con un intento fallido hoy no se vuelven a sumar hoy.
     */
    @Transactional(rollbackFor = Exception.class)
    public RutaDelDiaDTO generarRutaDeEntregas(String correoDistribuidor, UbicacionDTO inicio) throws MiException {
        Distribuidor distribuidor = distribuidor(correoDistribuidor);
        List<PuntoVenta> comercios = repositorioPedido
                .findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(distribuidor.getId(),
                        EstadoPedido.PENDIENTE_DE_ENTREGA)
                .stream().map(Pedido::getPuntoVenta).distinct().toList();
        if (servicioRuta.asegurarRutaEntrega(distribuidor, LocalDate.now(), comercios).isEmpty()) {
            throw new RecursoNoEncontradoException("No tenés pedidos pendientes de entrega asignados");
        }
        Function<PuntoVenta, List<PedidoResumenDTO>> pedidos = pedidosDeEntrega(distribuidor);
        boolean conUbicacion = inicio != null && inicio.latitud() != null && inicio.longitud() != null;
        return conUbicacion
                ? servicioRuta.ordenarRuta(distribuidor, TipoRuta.ENTREGA, inicio, pedidos)
                : servicioRuta.rutaDelDia(distribuidor, TipoRuta.ENTREGA, null, pedidos);
    }

    /** Ruta de entregas de hoy (solo lectura), con los pedidos de cada comercio. */
    @Transactional(readOnly = true)
    public RutaDelDiaDTO consultarRutaDeEntregas(String correoDistribuidor, UbicacionDTO ubicacion) throws MiException {
        Distribuidor distribuidor = distribuidor(correoDistribuidor);
        return servicioRuta.rutaDelDia(distribuidor, TipoRuta.ENTREGA, ubicacion, pedidosDeEntrega(distribuidor));
    }

    /** marcarEntregado del diagrama: entrega TODOS los pedidos pendientes del distribuidor en ese comercio. */
    @Transactional(rollbackFor = Exception.class)
    public List<PedidoResponseDTO> registrarEntrega(String correoDistribuidor, String idPuntoVenta) throws MiException {
        Distribuidor distribuidor = distribuidor(correoDistribuidor);
        Ruta ruta = rutaDeEntregasDeHoy(distribuidor);
        List<Pedido> pedidos = pedidosPendientesEn(distribuidor, idPuntoVenta);
        if (pedidos.isEmpty()) {
            throw new MiException("No tenés pedidos pendientes para entregar en ese comercio");
        }
        servicioRuta.registrarResultado(ruta, idPuntoVenta, ResultadoVisita.ENTREGADO, null);
        LocalDateTime ahora = LocalDateTime.now();
        List<PedidoResponseDTO> entregados = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            Pedido bloqueado = bloquear(pedido.getId());
            bloqueado.setEstado(EstadoPedido.ENTREGADO);
            bloqueado.setFechaEntrega(ahora);
            entregados.add(convertirAResponse(repositorioPedido.save(bloqueado)));
        }
        avisarProximoComercio(ruta, distribuidor);
        return entregados;
    }

    /**
     * Avisa por correo al comercio que el distribuidor visitará a continuación. Un fallo de correo no afecta
     * la entrega: ServicioCorreo registra el problema y sigue.
     */
    private void avisarProximoComercio(Ruta ruta, Distribuidor distribuidor) {
        String nombreDistribuidor = (distribuidor.getNombre() + " " + distribuidor.getApellido()).trim();
        servicioRuta.proximaParadaPendiente(ruta)
                .ifPresent(parada -> servicioCorreo.enviarAvisoProximaVisita(
                        parada.getPuntoVenta(), nombreDistribuidor));
    }

    /**
     * Entrega no concretada ("Siguiente punto" del distribuidor): se registra el intento y el motivo en la
     * parada. Los pedidos NO cambian de estado ni se libera stock: siguen pendientes para el día siguiente.
     */
    @Transactional(rollbackFor = Exception.class)
    public void registrarEntregaNoConcretada(String correoDistribuidor, String idPuntoVenta, String motivo)
            throws MiException {
        Distribuidor distribuidor = distribuidor(correoDistribuidor);
        Ruta ruta = rutaDeEntregasDeHoy(distribuidor);
        if (pedidosPendientesEn(distribuidor, idPuntoVenta).isEmpty()) {
            throw new MiException("No tenés pedidos pendientes para entregar en ese comercio");
        }
        servicioRuta.registrarResultado(ruta, idPuntoVenta, ResultadoVisita.NO_ENTREGADO, motivo);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Map<String, Integer> agruparLineas(List<LineaPedidoDTO> lineas) throws MiException {
        if (lineas == null || lineas.isEmpty()) {
            throw new MiException("El pedido debe tener al menos un producto");
        }
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (LineaPedidoDTO linea : lineas) {
            if (linea == null || estaVacio(linea.idProducto())) {
                throw new MiException("Cada renglón debe indicar el producto");
            }
            if (linea.cantidad() == null || linea.cantidad() <= 0) {
                throw new MiException("La cantidad de cada producto debe ser mayor a cero");
            }
            cantidades.merge(linea.idProducto(), linea.cantidad(), Integer::sum);
        }
        return cantidades;
    }

    /** El precio se toma del catálogo (precio vigente); si el producto no está disponible, no se puede pedir. */
    private DetallePedido crearDetalle(String idProducto, int cantidad) throws MiException {
        Producto producto = obtenerReferencia(servicioProducto, idProducto, "el producto");
        ProductoCatalogoDTO enCatalogo;
        try {
            enCatalogo = servicioCatalogo.consultarProductoDisponible(idProducto);
        } catch (RecursoNoEncontradoException e) {
            throw new MiException(producto.getNombre() + " no está disponible (sin stock o sin precio vigente)");
        }
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(enCatalogo.precio());
        detalle.setSubtotal(redondear(enCatalogo.precio() * cantidad));
        return detalle;
    }

    private Pedido bloquear(String id) throws MiException {
        if (estaVacio(id)) {
            throw new MiException("Debe indicar el pedido");
        }
        return repositorioPedido.buscarParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + id));
    }

    /** Un pedido de otro cliente se informa como inexistente: no se revela que existe. */
    private Pedido pedidoDelCliente(String correoCliente, Pedido pedido) throws MiException {
        Cliente cliente = servicioCliente.obtenerClientePorCorreo(correoCliente);
        if (!pedido.getPuntoVenta().getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNoEncontradoException("No existe el pedido con id " + pedido.getId());
        }
        return pedido;
    }

    private Distribuidor distribuidor(String correo) throws MiException {
        Empleado empleado = servicioEmpleado.obtenerEmpleadoPorCorreo(correo);
        if (!(empleado instanceof Distribuidor distribuidor)) {
            throw new MiException("La cuenta no pertenece a un distribuidor");
        }
        return distribuidor;
    }

    private Ruta rutaDeEntregasDeHoy(Distribuidor distribuidor) throws MiException {
        return servicioRuta.buscarRutaDelDia(distribuidor, TipoRuta.ENTREGA, LocalDate.now())
                .orElseThrow(() -> new MiException("Primero generá tu ruta de entregas de hoy"));
    }

    private List<Pedido> pedidosPendientesEn(Distribuidor distribuidor, String idPuntoVenta) {
        return repositorioPedido.findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(
                        distribuidor.getId(), EstadoPedido.PENDIENTE_DE_ENTREGA).stream()
                .filter(p -> p.getPuntoVenta().getId().equals(idPuntoVenta))
                .toList();
    }

    /** Pedidos de cada comercio para la ruta de entregas: los pendientes del distribuidor y los que entregó hoy. */
    private Function<PuntoVenta, List<PedidoResumenDTO>> pedidosDeEntrega(Distribuidor distribuidor) {
        List<Pedido> pendientes = repositorioPedido.findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(
                distribuidor.getId(), EstadoPedido.PENDIENTE_DE_ENTREGA);
        LocalDate hoy = LocalDate.now();
        List<Pedido> entregadosHoy = repositorioPedido.findByDistribuidor_IdAndEstadoAndEliminadoFalseOrderByFechaHoraCargaAsc(
                        distribuidor.getId(), EstadoPedido.ENTREGADO).stream()
                .filter(p -> p.getFechaEntrega() != null && p.getFechaEntrega().toLocalDate().equals(hoy))
                .toList();
        List<Pedido> todos = new ArrayList<>(pendientes);
        todos.addAll(entregadosHoy);
        Map<String, FacturaCliente> facturas = servicioFacturaCliente.facturasDePedidos(
                todos.stream().map(Pedido::getId).toList());
        return puntoVenta -> todos.stream()
                .filter(p -> p.getPuntoVenta().getId().equals(puntoVenta.getId()))
                .map(p -> new PedidoResumenDTO(p.getId(), p.getNumeroPedido(), p.getEstado(),
                        p.getDetalles().stream().mapToInt(DetallePedido::getCantidad).sum(),
                        // A cobrar: el total facturado (con IVA); sin factura (datos anteriores) el del pedido
                        facturas.containsKey(p.getId()) ? facturas.get(p.getId()).getTotal() : p.getTotal()))
                .toList();
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // ------------------------------------------------------------------
    // Estadísticas y consultas para otros módulos
    // ------------------------------------------------------------------

    /**
     * Pedidos por preventista por día de un mes (gráfico de barras HU-43). Los datos salen de la base
     * (pedidos no anulados, por fecha de carga); acá solo se agrupan por preventista y día.
     */
    @Transactional(readOnly = true)
    public com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO pedidosPorPreventistaYDia(java.time.YearMonth mes) {
        java.time.YearMonth periodo = mes == null ? java.time.YearMonth.now() : mes;
        int dias = periodo.lengthOfMonth();
        List<Object[]> filas = repositorioPedido.pedidosCargadosEntre(periodo.atDay(1).atStartOfDay(),
                periodo.plusMonths(1).atDay(1).atStartOfDay(), EstadoPedido.ANULADO);
        Map<String, String> nombres = new java.util.TreeMap<>();
        Map<String, long[]> conteos = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            String id = (String) fila[0];
            nombres.putIfAbsent(id, (fila[1] + " " + fila[2]).trim());
            int dia = ((LocalDateTime) fila[3]).getDayOfMonth();
            conteos.computeIfAbsent(id, clave -> new long[dias])[dia - 1]++;
        }
        List<com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO.Serie> series = conteos.entrySet().stream()
                .map(e -> new com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO.Serie(e.getKey(),
                        nombres.get(e.getKey()),
                        java.util.Arrays.stream(e.getValue()).boxed().toList(),
                        java.util.Arrays.stream(e.getValue()).sum()))
                .sorted(java.util.Comparator.comparing(com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO.Serie::preventista))
                .toList();
        return new com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO(periodo.toString(),
                java.util.stream.IntStream.rangeClosed(1, dias).boxed().toList(), series, filas.size());
    }

    /** ¿El producto está en pedidos sin terminar? Lo consulta ServicioProducto antes de darlo de baja. */
    @Transactional(readOnly = true)
    public boolean hayPedidosAbiertosConProducto(String idProducto) {
        return repositorioPedido.existenPedidosConProducto(idProducto,
                List.of(EstadoPedido.CARGADO, EstadoPedido.PENDIENTE_DE_ENTREGA));
    }
}
