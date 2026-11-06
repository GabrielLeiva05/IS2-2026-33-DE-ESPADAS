package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoLineaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoLineaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.LineaPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.ReservaStock;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioReservaStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ServicioPedido {

    private static final int ESCALA_MONETARIA = 2;
    private final RepositorioPedido repositorioPedido;
    private final RepositorioReservaStock repositorioReservaStock;
    private final RepositorioPago repositorioPago;
    private final ServicioPuntoVenta servicioPuntoVenta;
    private final ServicioProducto servicioProducto;
    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioStock servicioStock;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpleado servicioEmpleado;

    public ServicioPedido(RepositorioPedido repositorioPedido, RepositorioReservaStock repositorioReservaStock,
                          RepositorioPago repositorioPago,
                          ServicioPuntoVenta servicioPuntoVenta, ServicioProducto servicioProducto,
                          ServicioVigenciaPrecio servicioVigenciaPrecio, ServicioStock servicioStock,
                          ServicioUsuario servicioUsuario, ServicioEmpleado servicioEmpleado) {
        this.repositorioPedido = repositorioPedido;
        this.repositorioReservaStock = repositorioReservaStock;
        this.repositorioPago = repositorioPago;
        this.servicioPuntoVenta = servicioPuntoVenta;
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioStock = servicioStock;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpleado = servicioEmpleado;
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO crearBorrador(PedidoRequestDTO dto, String correoPreventista) throws MiException {
        validarSolicitud(dto);
        usuarioPreventista(correoPreventista);
        var punto = servicioPuntoVenta.obtenerPuntoPropio(dto.idPuntoVenta(), correoPreventista);
        Pedido pedido = new Pedido();
        pedido.setPuntoVenta(punto);
        pedido.setPreventista(punto.getPreventista());
        aplicarLineas(pedido, dto.lineas());
        return convertir(repositorioPedido.save(pedido));
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO actualizarBorrador(String idPedido, PedidoRequestDTO dto, String correoPreventista)
            throws MiException {
        validarSolicitud(dto);
        Usuario usuario = usuarioPreventista(correoPreventista);
        Pedido pedido = repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse(idPedido,
                        servicioEmpleado.buscarPorUsuario(usuario.getId()).orElseThrow().getId())
                .orElseThrow(() -> new MiException("El pedido no pertenece al preventista o no existe"));
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new MiException("Solo se pueden modificar pedidos en borrador");
        }
        if (!Objects.equals(pedido.getPuntoVenta().getId(), dto.idPuntoVenta())) {
            throw new MiException("No se puede cambiar el punto de venta de un pedido existente");
        }
        pedido.getLineas().forEach(linea -> linea.setEliminado(true));
        pedido.getLineas().clear();
        repositorioPedido.flush();
        aplicarLineas(pedido, dto.lineas());
        return convertir(repositorioPedido.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarMisPedidos(String correoPreventista) throws MiException {
        Usuario usuario = usuarioPreventista(correoPreventista);
        String idPreventista = servicioEmpleado.buscarPorUsuario(usuario.getId()).orElseThrow().getId();
        return repositorioPedido.findByPreventista_IdAndEliminadoFalseOrderByIdAsc(idPreventista)
                .stream().map(this::convertir).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO confirmar(String idPedido, String correoPreventista) throws MiException {
        Usuario usuario = usuarioPreventista(correoPreventista);
        String idPreventista = servicioEmpleado.buscarPorUsuario(usuario.getId()).orElseThrow().getId();
        Pedido pedido = repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse(idPedido, idPreventista)
                .orElseThrow(() -> new MiException("El pedido no pertenece al preventista o no existe"));
        if (pedido.getEstado() != EstadoPedido.BORRADOR || pedido.getLineas().isEmpty()) {
            throw new MiException("Solo se puede confirmar un pedido en borrador con productos");
        }
        List<LineaPedido> lineas = pedido.getLineas().stream()
                .sorted(Comparator.comparing(linea -> linea.getProducto().getId())).toList();
        for (LineaPedido linea : lineas) {
            servicioStock.bloquearParaReserva(linea.getProducto().getId());
        }
        List<ReservaStock> reservas = new ArrayList<>();
        for (LineaPedido linea : lineas) {
            int unidadesPorPresentacion = linea.getProducto().getUnidadesPorPresentacion();
            if (unidadesPorPresentacion <= 0) {
                throw new MiException("El producto no tiene una presentación válida: " + linea.getNombreProducto());
            }
            int unidadesSolicitadas;
            try {
                unidadesSolicitadas = Math.multiplyExact(linea.getCantidadPresentaciones(), unidadesPorPresentacion);
            } catch (ArithmeticException e) {
                throw new MiException("La cantidad solicitada supera el máximo permitido");
            }
            int disponible = servicioStock.cantidadDisponibleParaPedido(linea.getProducto().getId(), pedido.getId());
            if (disponible < unidadesSolicitadas) {
                throw new MiException("Stock insuficiente para " + linea.getNombreProducto()
                        + ": disponible " + disponible + " unidades, solicitado " + unidadesSolicitadas);
            }
            ReservaStock reserva = new ReservaStock();
            reserva.setPedido(pedido);
            reserva.setProducto(linea.getProducto());
            reserva.setCantidad(unidadesSolicitadas);
            reserva.setFechaCreacion(LocalDateTime.now());
            reservas.add(reserva);
        }
        for (ReservaStock reserva : reservas) {
            repositorioReservaStock.save(reserva);
        }
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setFechaConfirmacion(LocalDateTime.now());
        return convertir(repositorioPedido.save(pedido));
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO cancelar(String idPedido, String correoPreventista) throws MiException {
        Usuario usuario = usuarioPreventista(correoPreventista);
        String idPreventista = servicioEmpleado.buscarPorUsuario(usuario.getId()).orElseThrow().getId();
        Pedido pedido = repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse(idPedido, idPreventista)
                .orElseThrow(() -> new MiException("El pedido no pertenece al preventista o no existe"));
        return cancelarReservas(pedido);
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO cancelarAdministrativo(String idPedido) throws MiException {
        Pedido pedido = repositorioPedido.findByIdAndEliminadoFalse(idPedido)
                .orElseThrow(() -> new MiException("No existe el pedido indicado"));
        return cancelarReservas(pedido);
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO completarAdministrativo(String idPedido) throws MiException {
        Pedido pedido = repositorioPedido.findByIdAndEliminadoFalse(idPedido)
                .orElseThrow(() -> new MiException("No existe el pedido indicado"));
        if (repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido, EstadoPago.ACREDITADO).isEmpty()) {
            throw new MiException("El pedido no tiene el pago total acreditado: no se puede consumir el stock");
        }
        return consumirReservasPorPago(pedido);
    }

    /** Consumo de stock disparado por la acreditación del pago total (Fase 3, ítem 7). */
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO consumirReservasPorPago(String idPedido) throws MiException {
        return consumirReservasPorPago(obtenerPedido(idPedido));
    }

    private PedidoResponseDTO consumirReservasPorPago(Pedido pedido) throws MiException {
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new MiException("Solo se puede completar un pedido confirmado");
        }
        List<ReservaStock> reservas = repositorioReservaStock.findByPedido_IdAndEliminadoFalse(pedido.getId()).stream()
                .filter(reserva -> reserva.getFechaLiberacion() == null && !reserva.isConsumida()).toList();
        if (reservas.size() != pedido.getLineas().size()) {
            throw new MiException("El pedido no tiene todas sus reservas de stock activas");
        }
        for (ReservaStock reserva : reservas) {
            servicioStock.consumirReserva(pedido.getId(), reserva.getProducto().getId(), reserva.getCantidad());
        }
        pedido.setEstado(EstadoPedido.COMPLETADO);
        pedido.setFechaCompletado(LocalDateTime.now());
        return convertir(repositorioPedido.save(pedido));
    }

    /** Cancelación idempotente cuando vence el pago: libera la reserva si el pedido sigue confirmado. */
    @Transactional(rollbackFor = Exception.class)
    public void cancelarPorVencimientoDePago(String idPedido) throws MiException {
        Pedido pedido = repositorioPedido.findByIdAndEliminadoFalse(idPedido).orElse(null);
        if (pedido == null || pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            return;
        }
        if (repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido, EstadoPago.ACREDITADO).isPresent()) {
            return;
        }
        cancelarReservas(pedido);
    }

    /** Bandeja administrativa: todos los pedidos, los más recientes primero. */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return repositorioPedido.findByEliminadoFalseOrderByFechaCreacionDesc().stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerDetalle(String idPedido) throws MiException {
        return convertir(obtenerPedido(idPedido));
    }

    /** Pedido de la cartera del preventista autenticado (autorización por ownership). */
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPropio(String idPedido, String correoPreventista) throws MiException {
        Usuario usuario = usuarioPreventista(correoPreventista);
        String idPreventista = servicioEmpleado.buscarPorUsuario(usuario.getId()).orElseThrow().getId();
        return repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse(idPedido, idPreventista)
                .orElseThrow(() -> new MiException("El pedido no pertenece al preventista o no existe"));
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedido(String idPedido) throws MiException {
        return repositorioPedido.findByIdAndEliminadoFalse(idPedido)
                .orElseThrow(() -> new MiException("No existe el pedido indicado"));
    }

    private PedidoResponseDTO cancelarReservas(Pedido pedido) throws MiException {
        if (pedido.getEstado() != EstadoPedido.BORRADOR && pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new MiException("El pedido ya no se puede cancelar desde su estado actual");
        }
        for (ReservaStock reserva : repositorioReservaStock.findByPedido_IdAndEliminadoFalse(pedido.getId())) {
            if (reserva.getFechaLiberacion() == null && !reserva.isConsumida()) {
                reserva.setFechaLiberacion(LocalDateTime.now());
            }
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCancelacion(LocalDateTime.now());
        return convertir(repositorioPedido.save(pedido));
    }

    private void aplicarLineas(Pedido pedido, List<PedidoLineaRequestDTO> lineas) throws MiException {
        BigDecimal total = BigDecimal.ZERO.setScale(ESCALA_MONETARIA);
        for (PedidoLineaRequestDTO dto : lineas) {
            if (dto == null || dto.idProducto() == null || dto.idProducto().isBlank()
                    || dto.cantidadPresentaciones() == null || dto.cantidadPresentaciones() <= 0) {
                throw new MiException("Cada línea debe indicar producto y cantidad mayor a cero");
            }
            Producto producto = servicioProducto.obtenerEntidad(dto.idProducto());
            if (producto.isEliminado()) throw new MiException("El producto ya no está disponible");
            if (pedido.getLineas().stream().anyMatch(linea -> linea.getProducto().getId().equals(producto.getId()))) {
                throw new MiException("No se puede repetir un producto en el mismo pedido");
            }
            VigenciaPrecio vigencia = servicioVigenciaPrecio.obtenerVigenciaVigente(producto.getId())
                    .orElseThrow(() -> new MiException("El producto no tiene precio vigente: " + producto.getNombre()));
            BigDecimal precio = BigDecimal.valueOf(vigencia.getPrecio()).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            int unidadesPorPresentacion = producto.getUnidadesPorPresentacion();
            if (unidadesPorPresentacion <= 0) {
                throw new MiException("El producto no tiene una presentación válida: " + producto.getNombre());
            }
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(dto.cantidadPresentaciones()))
                    .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
            LineaPedido linea = new LineaPedido();
            linea.setPedido(pedido);
            linea.setProducto(producto);
            linea.setNombreProducto(producto.getNombre());
            linea.setPrecioUnitario(precio);
            linea.setCantidadPresentaciones(dto.cantidadPresentaciones());
            linea.setSubtotal(subtotal);
            pedido.getLineas().add(linea);
            total = total.add(subtotal);
        }
        pedido.setTotal(total.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP));
    }

    private PedidoResponseDTO convertir(Pedido pedido) {
        List<PedidoLineaResponseDTO> lineas = pedido.getLineas().stream()
                .map(linea -> new PedidoLineaResponseDTO(linea.getProducto().getId(), linea.getNombreProducto(),
                        linea.getCantidadPresentaciones(), linea.getPrecioUnitario(), linea.getSubtotal())).toList();
        return new PedidoResponseDTO(pedido.getId(), pedido.getPuntoVenta().getId(),
                pedido.getPuntoVenta().getNombreComercial(), pedido.getPreventista().getId(), pedido.getEstado(),
                lineas, pedido.getTotal(), pedido.getFechaCreacion(), pedido.getFechaConfirmacion(),
                pedido.getFechaCancelacion(), pedido.getFechaCompletado());
    }

    private Usuario usuarioPreventista(String correo) throws MiException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new MiException("No se encontró la cuenta autenticada"));
        if (usuario.getRol() != RolUsuario.PREVENTISTA && usuario.getRol() != RolUsuario.JEFE) {
            throw new MiException("La cuenta autenticada no puede operar pedidos presenciales");
        }
        return usuario;
    }

    private static void validarSolicitud(PedidoRequestDTO dto) throws MiException {
        if (dto == null || dto.idPuntoVenta() == null || dto.idPuntoVenta().isBlank()
                || dto.lineas() == null || dto.lineas().isEmpty()) {
            throw new MiException("Debe indicar punto de venta y al menos un producto");
        }
    }
}
