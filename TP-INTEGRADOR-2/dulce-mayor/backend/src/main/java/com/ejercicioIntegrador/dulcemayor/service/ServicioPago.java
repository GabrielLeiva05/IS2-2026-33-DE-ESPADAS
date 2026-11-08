package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.pagos.DatosInicioPago;
import com.ejercicioIntegrador.dulcemayor.pagos.EstrategiaNotificable;
import com.ejercicioIntegrador.dulcemayor.pagos.EstrategiaPago;
import com.ejercicioIntegrador.dulcemayor.pagos.NotificacionPago;
import com.ejercicioIntegrador.dulcemayor.pagos.ResolvedorEstrategiaPago;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestión de pagos del pedido (Fase 3, ítems 5 a 7). El estado del pago se lleva por separado del
 * estado del pedido: solo la acreditación del total consume la reserva de stock y emite el
 * comprobante, dentro de la misma transacción.
 */
@Service
public class ServicioPago {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioPago.class);

    private final RepositorioPago repositorioPago;
    private final ResolvedorEstrategiaPago estrategias;
    private final ServicioPedido servicioPedido;
    private final ServicioComprobante servicioComprobante;
    private final com.ejercicioIntegrador.dulcemayor.pagos.ServicioQr servicioQr;
    private final ServicioCorreo servicioCorreo;
    private final org.springframework.context.ApplicationEventPublisher eventos;

    public ServicioPago(RepositorioPago repositorioPago, ResolvedorEstrategiaPago estrategias,
                        ServicioPedido servicioPedido, ServicioComprobante servicioComprobante,
                        com.ejercicioIntegrador.dulcemayor.pagos.ServicioQr servicioQr,
                        ServicioCorreo servicioCorreo,
                        org.springframework.context.ApplicationEventPublisher eventos) {
        this.repositorioPago = repositorioPago;
        this.estrategias = estrategias;
        this.servicioPedido = servicioPedido;
        this.servicioComprobante = servicioComprobante;
        this.servicioQr = servicioQr;
        this.servicioCorreo = servicioCorreo;
        this.eventos = eventos;
    }

    // ------------------------------------------------------------------ alta del cobro

    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO iniciarPorPreventista(String idPedido, PagoRequestDTO dto, String correoPreventista)
            throws MiException {
        return iniciar(servicioPedido.obtenerPedidoPropio(idPedido, correoPreventista), dto);
    }

    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO iniciarPorAdministrativo(String idPedido, PagoRequestDTO dto) throws MiException {
        return iniciar(servicioPedido.obtenerPedido(idPedido), dto);
    }

    private PagoResponseDTO iniciar(Pedido pedido, PagoRequestDTO dto) throws MiException {
        if (dto == null || dto.medio() == null) {
            throw new MiException("Indicá el medio de pago");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new MiException("El pedido está cancelado y no admite pagos");
        }
        if (pedido.getEstado() == EstadoPedido.COMPLETADO
                || repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(pedido.getId(),
                        EstadoPago.ACREDITADO).isPresent()) {
            throw new MiException("El pedido ya tiene el pago total acreditado");
        }
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new MiException("Solo se puede pagar un pedido confirmado");
        }
        expirarPendienteVencido(pedido.getId());
        Optional<Pago> pendiente = repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(pedido.getId(),
                EstadoPago.PENDIENTE);
        if (pendiente.isPresent()) {
            throw new MiException("El pedido ya tiene un pago pendiente con "
                    + pendiente.get().getMedio() + " hasta el " + pendiente.get().getFechaVencimiento());
        }

        EstrategiaPago estrategia = estrategias.obtener(dto.medio());
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMedio(dto.medio());
        pago.setImporte(pedido.getTotal().setScale(2, RoundingMode.HALF_UP));
        pago.setEstado(EstadoPago.PENDIENTE);
        // La clave de idempotencia se define antes de llamar al proveedor: viaja como external_reference.
        pago.setReferenciaOperacion(UUID.randomUUID().toString());
        DatosInicioPago datos = estrategia.iniciar(pago);
        pago.setFechaVencimiento(LocalDateTime.now().plus(estrategia.vigenciaReserva()));
        pago.setQrContenido(datos.qrContenido());
        pago.setReferenciaExterna(datos.referenciaExterna());
        Pago guardado = repositorioPago.save(pago);
        if (guardado.getQrContenido() != null) {
            enviarQrAlCliente(guardado, datos.instrucciones());
        }
        return convertir(guardado, datos.instrucciones());
    }

    /** Cobro en efectivo registrado por administración (interino hasta la app del distribuidor). */
    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO registrarEfectivo(String idPedido) throws MiException {
        PagoResponseDTO creado = iniciar(servicioPedido.obtenerPedido(idPedido),
                new PagoRequestDTO(MedioPago.EFECTIVO));
        return acreditar(creado.id(), new ConfirmarPagoRequestDTO(null, "Cobro en efectivo registrado por administración"));
    }

    // ------------------------------------------------------------------ acreditación

    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO acreditar(String idPago, ConfirmarPagoRequestDTO dto) throws MiException {
        Pago pago = repositorioPago.buscarParaActualizar(idPago)
                .orElseThrow(() -> new MiException("No existe el pago indicado"));
        if (pago.getEstado() == EstadoPago.ACREDITADO) {
            return convertir(pago, "El pago ya estaba acreditado");
        }
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new MiException("El pago está " + pago.getEstado() + " y no se puede acreditar");
        }
        if (estaVencido(pago)) {
            expirar(pago);
            throw new MiException("El pago venció: la reserva de stock fue liberada");
        }

        String referencia = dto == null ? null : recortar(dto.referencia());
        String observacion = dto == null ? null : recortar(dto.observacion());
        estrategias.obtener(pago.getMedio()).acreditar(pago, referencia, observacion);

        pago.setEstado(EstadoPago.ACREDITADO);
        pago.setFechaAcreditacion(LocalDateTime.now());
        if (referencia != null) {
            pago.setReferenciaExterna(referencia);
        }
        if (observacion != null) {
            pago.setObservacion(observacion);
        }
        repositorioPago.save(pago);

        servicioPedido.consumirReservasPorPago(pago.getPedido().getId());
        var comprobante = servicioComprobante.emitir(pago);
        notificarPagoAcreditado(pago, comprobante == null ? null : comprobante.getNumero());
        return convertir(pago, "Pago acreditado: se consumió el stock y se emitió el comprobante interno");
    }

    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO rechazar(String idPago, ConfirmarPagoRequestDTO dto) throws MiException {
        Pago pago = repositorioPago.buscarParaActualizar(idPago)
                .orElseThrow(() -> new MiException("No existe el pago indicado"));
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new MiException("Solo se puede rechazar un pago pendiente");
        }
        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setObservacion(dto == null ? null : recortar(dto.observacion()));
        return convertir(repositorioPago.save(pago), "Pago marcado como rechazado");
    }

    // ------------------------------------------------------------------ vencimiento

    @Transactional(readOnly = true)
    public List<String> pagosVencidos() {
        return repositorioPago
                .findByEstadoAndFechaVencimientoBeforeAndEliminadoFalse(EstadoPago.PENDIENTE, LocalDateTime.now())
                .stream().map(Pago::getId).toList();
    }

    /**
     * Expira un pago vencido y libera la reserva. Toma el pago con bloqueo pesimista para no
     * pisarse con una acreditación concurrente (webhook o conciliación manual).
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean expirar(String idPago) throws MiException {
        Pago pago = repositorioPago.buscarParaActualizar(idPago).orElse(null);
        if (pago == null || pago.getEstado() != EstadoPago.PENDIENTE || !estaVencido(pago)) {
            return false;
        }
        expirar(pago);
        LOG.info("Pago {} del pedido {} expirado por vencimiento", pago.getId(), pago.getPedido().getId());
        return true;
    }

    private void expirar(Pago pago) throws MiException {
        pago.setEstado(EstadoPago.EXPIRADO);
        repositorioPago.save(pago);
        servicioPedido.cancelarPorVencimientoDePago(pago.getPedido().getId());
    }

    private void expirarPendienteVencido(String idPedido) throws MiException {
        repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido, EstadoPago.PENDIENTE)
                .filter(this::estaVencido)
                .ifPresent(pago -> {
                    pago.setEstado(EstadoPago.EXPIRADO);
                    repositorioPago.save(pago);
                });
    }

    // ------------------------------------------------------------------ notificaciones del proveedor

    @Transactional(rollbackFor = Exception.class)
    public Optional<PagoResponseDTO> procesarNotificacion(String firma, String requestId, String dataId, String tipo)
            throws MiException {
        EstrategiaNotificable notificable = estrategias.notificable(MedioPago.MERCADO_PAGO);
        NotificacionPago notificacion = notificable.resolver(firma, requestId, dataId, tipo);
        if (notificacion == null || notificacion.referenciaOperacion() == null) {
            return Optional.empty();
        }
        Pago pago = repositorioPago
                .findFirstByReferenciaOperacionAndEliminadoFalse(notificacion.referenciaOperacion()).orElse(null);
        if (pago == null) {
            LOG.warn("Notificación de Mercado Pago sin pago asociado: {}", notificacion.referenciaOperacion());
            return Optional.empty();
        }
        if (!notificacion.acreditado()) {
            return Optional.of(convertir(pago, "Mercado Pago informa el pago como pendiente o rechazado"));
        }
        return Optional.of(acreditar(pago.getId(),
                new ConfirmarPagoRequestDTO(notificacion.referenciaExterna(), "Acreditado por notificación de Mercado Pago")));
    }

    // ------------------------------------------------------------------ consultas

    /**
     * Cobro en efectivo al entregar: acredita el intento pendiente de efectivo o crea uno nuevo.
     * Si el pedido ya estaba pago, devuelve ese pago sin tocar el stock.
     */
    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO cobrarEnEntrega(String idPedido) throws MiException {
        Optional<Pago> pendiente = repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido,
                EstadoPago.PENDIENTE);
        if (pendiente.isPresent() && pendiente.get().getMedio() == MedioPago.EFECTIVO) {
            return acreditar(pendiente.get().getId(),
                    new ConfirmarPagoRequestDTO(null, "Cobro en efectivo registrado al entregar"));
        }
        Optional<Pago> acreditado = repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido,
                EstadoPago.ACREDITADO);
        if (acreditado.isPresent()) {
            return convertir(acreditado.get(), "El pedido ya tenía el pago acreditado");
        }
        return registrarEfectivo(idPedido);
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPorPedido(String idPedido) {
        return repositorioPago.findByPedido_IdAndEliminadoFalseOrderByFechaCreacionDesc(idPedido).stream()
                .map(pago -> convertir(pago, null)).toList();
    }

    @Transactional(readOnly = true)
    public Optional<PagoResponseDTO> buscarAcreditado(String idPedido) {
        return repositorioPago.findFirstByPedido_IdAndEstadoAndEliminadoFalse(idPedido, EstadoPago.ACREDITADO)
                .map(pago -> convertir(pago, null));
    }

    private boolean estaVencido(Pago pago) {
        return pago.getFechaVencimiento() != null && pago.getFechaVencimiento().isBefore(LocalDateTime.now());
    }

    /** Cobro con tarjeta por Checkout API: sin redirección, con el token que generó el navegador. */
    @Transactional(rollbackFor = Exception.class)
    public PagoResponseDTO cobrarConTarjeta(String idPago, com.ejercicioIntegrador.dulcemayor.dto.PagoTarjetaRequestDTO dto)
            throws MiException {
        if (dto == null) {
            throw new MiException("Faltan los datos de la tarjeta");
        }
        Pago pago = repositorioPago.buscarParaActualizar(idPago)
                .orElseThrow(() -> new MiException("No existe el pago indicado"));
        if (pago.getEstado() == EstadoPago.ACREDITADO) {
            return convertir(pago, "El pago ya estaba acreditado");
        }
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new MiException("El pago está " + pago.getEstado() + " y no se puede cobrar");
        }
        if (estaVencido(pago)) {
            expirar(pago);
            throw new MiException("El pago venció: la reserva de stock fue liberada");
        }
        var resultado = estrategias.cobroConTarjeta(pago.getMedio()).cobrarConTarjeta(pago,
                new com.ejercicioIntegrador.dulcemayor.pagos.ClienteMercadoPago.DatosTarjeta(dto.token(),
                        dto.paymentMethodId(), dto.installments(), dto.issuerId(), dto.email(),
                        dto.tipoDocumento(), dto.numeroDocumento()));
        if (!resultado.aprobado()) {
            pago.setObservacion("Cobro con tarjeta rechazado: " + resultado.estado());
            repositorioPago.save(pago);
            throw new MiException("Mercado Pago no aprobó el pago (estado " + resultado.estado() + ")");
        }
        return acreditar(pago.getId(),
                new ConfirmarPagoRequestDTO(resultado.idPagoExterno(), "Cobro con tarjeta por Checkout API"));
    }

    /** Imagen del QR lista para mostrar en la aplicación (data URI) o null si el pago no lo tiene. */
    private String imagenQr(Pago pago) {
        if (pago.getQrContenido() == null || pago.getQrContenido().isBlank()) {
            return null;
        }
        try {
            return servicioQr.dataUri(pago.getQrContenido());
        } catch (MiException e) {
            return null;
        }
    }

    /**
     * Envía al cliente el correo con el QR incrustado, apenas el preventista confirma que va a pagar
     * con Mercado Pago. Si el envío falla, el cobro sigue vigente: el QR también está en la app.
     */
    private void enviarQrAlCliente(Pago pago, String instrucciones) {
        var pedido = pago.getPedido();
        String correo = pedido.getPuntoVenta() == null ? null : pedido.getPuntoVenta().getEmailContacto();
        if (correo == null || correo.isBlank()) {
            return;
        }
        String comercio = pedido.getPuntoVenta().getNombreComercial();
        try {
            byte[] imagen = servicioQr.generarPng(pago.getQrContenido());
            String html = """
                    <html><body style="font-family:Arial,sans-serif;color:#1f2937">
                      <h2 style="color:#7a35e0">Pedido confirmado — %s</h2>
                      <p>Hola:</p>
                      <p>Confirmamos el pedido <strong>%s</strong> por un total de
                         <strong>$ %s</strong> y reservamos el stock.</p>
                      <p>Para pagar con Mercado Pago, escaneá este QR desde la app
                         (o mostralo en el comercio):</p>
                      <p><img src="cid:qr-pago" alt="QR de pago de Mercado Pago" width="320" height="320"></p>
                      <p style="color:#5d5b70;font-size:13px">%s</p>
                      <p>También podés pagar con tarjeta desde la app del preventista, sin salir de la pantalla.</p>
                      <p>— Dulce Mayor</p>
                    </body></html>
                    """.formatted(escapar(comercio), pedido.getId(), pago.getImporte().toPlainString(),
                    escapar(instrucciones == null ? "" : instrucciones));
            servicioCorreo.enviarHtml(correo, "Pedido confirmado — pagá con Mercado Pago",
                    html, com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo.PEDIDO_CONFIRMADO,
                    java.util.Map.of("qr-pago", imagen));
        } catch (RuntimeException | MiException e) {
            LOG.warn("No se pudo enviar el QR por correo para el pago {}: {}", pago.getId(), e.getMessage());
        }
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private PagoResponseDTO convertir(Pago pago, String instrucciones) {
        return new PagoResponseDTO(pago.getId(), pago.getPedido().getId(), pago.getMedio(), pago.getEstado(),
                pago.getImporte(), pago.getReferenciaOperacion(), pago.getReferenciaExterna(), instrucciones,
                pago.getQrContenido(), imagenQr(pago), pago.getFechaCreacion(), pago.getFechaVencimiento(),
                pago.getFechaAcreditacion(), pago.getObservacion());
    }

    private static String recortar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    /** Avisa al cliente que el pago quedó acreditado y su comprobante está disponible. */
    private void notificarPagoAcreditado(Pago pago, String numeroComprobante) {
        var pedido = pago.getPedido();
        String correo = pedido.getPuntoVenta() == null ? null : pedido.getPuntoVenta().getEmailContacto();
        String comercio = pedido.getPuntoVenta() == null ? null : pedido.getPuntoVenta().getNombreComercial();
        eventos.publishEvent(new com.ejercicioIntegrador.dulcemayor.eventos.EventoPedido(pedido.getId(),
                com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo.PAGO_ACREDITADO, correo, comercio,
                numeroComprobante == null ? null : "Comprobante interno: " + numeroComprobante));
    }
}