package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPago;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.FormaDePago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.FacturaCliente;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPago;
import com.ejercicioIntegrador.dulcemayor.service.pagos.GeneradorQrPng;
import com.ejercicioIntegrador.dulcemayor.service.pagos.MercadoPagoQrCliente;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pagos de pedidos (Checkout API de Mercado Pago para QR; transferencia y efectivo informados por correo).
 *
 * Flujo:
 *  1. registrarPagoDePedido(): el preventista confirmó el pedido y eligió el medio de pago. Se crea el Pago
 *     (PENDIENTE) y se envía el correo al cliente con el detalle, el total y las instrucciones:
 *       - EFECTIVO: aviso de pago al entregar.
 *       - TRANSFERENCIA: alias y titular.
 *       - QR: se crea la cobranza en Mercado Pago y se envía el QR adjunto.
 *  2. procesarNotificacion(): webhook de Mercado Pago. Se consulta el estado real en la API; si el pago está
 *     aprobado, el Pago pasa a APROBADO, la FacturaCliente a PAGADA y se envía el correo de confirmación.
 *
 * La operación es idempotente: una segunda notificación del mismo pago no vuelve a acreditar ni a enviar correo.
 */
@Service
public class ServicioPago {

    private static final Logger log = LoggerFactory.getLogger(ServicioPago.class);
    /** Jackson 2 no se expone como bean en Spring Boot 4; para leer el webhook alcanza una instancia propia. */
    private static final ObjectMapper LECTOR_JSON = new ObjectMapper();

    private final RepositorioPago repositorioPago;
    private final MercadoPagoQrCliente mercadoPago;
    private final GeneradorQrPng generadorQr;
    private final ServicioCorreo servicioCorreo;
    private final ServicioFacturaCliente servicioFacturaCliente;

    @Value("${app.pagos.alias-transferencia:dulcemayor.mp}")
    private String aliasTransferencia;

    public ServicioPago(RepositorioPago repositorioPago, MercadoPagoQrCliente mercadoPago,
                        GeneradorQrPng generadorQr, ServicioCorreo servicioCorreo,
                        ServicioFacturaCliente servicioFacturaCliente) {
        this.repositorioPago = repositorioPago;
        this.mercadoPago = mercadoPago;
        this.generadorQr = generadorQr;
        this.servicioCorreo = servicioCorreo;
        this.servicioFacturaCliente = servicioFacturaCliente;
    }

    /**
     * Registra el pago del pedido recién confirmado y avisa al cliente por correo.
     * Si el medio es QR y Mercado Pago falla, se lanza MiException para que la confirmación completa se
     * deshaga (el preventista puede reintentar). Un fallo de correo no revierte nada (ServicioCorreo lo absorbe).
     */
    @Transactional(rollbackFor = Exception.class)
    public Pago registrarPagoDePedido(Pedido pedido, FacturaCliente factura, FormaDePago forma) throws MiException {
        if (forma == null) {
            throw new MiException("Debe indicar el medio de pago preferido del cliente");
        }
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setFormaDePago(forma);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMonto(factura.getTotal());
        pago.setFechaCreacion(LocalDateTime.now());

        byte[] qrPng = null;
        if (forma == FormaDePago.TRANSFERENCIA) {
            pago.setAliasTransferencia(aliasTransferencia);
        } else if (forma == FormaDePago.QR) {
            MercadoPagoQrCliente.QrGenerado qr = mercadoPago.crearCobranza(
                    pedido.getId(),
                    "Pedido N.º " + pedido.getNumeroPedido(),
                    "Dulce Mayor - " + pedido.getPuntoVenta().getNombreFantasia(),
                    factura.getTotal());
            pago.setQrData(qr.qrData());
            pago.setIdOrdenMp(qr.idOrden());
            qrPng = generadorQr.generar(qr.qrData());
        }

        Pago guardado = repositorioPago.save(pago);
        servicioCorreo.enviarConfirmacionPedido(pedido, factura, forma, aliasTransferencia, qrPng);
        return guardado;
    }

    /**
     * Procesa una notificación (webhook) de Mercado Pago. Nunca propaga la excepción: Mercado Pago reintenta
     * si respondemos error, y preferimos registrar y contestar 200.
     *
     * @return true si el pago quedó aprobado en esta notificación.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean procesarNotificacion(Map<String, String> parametros, String cuerpo) {
        try {
            String tipo = primeroNoVacio(parametros.get("type"), parametros.get("topic"));
            String id = primeroNoVacio(parametros.get("data.id"), parametros.get("id"));

            if (cuerpo != null && !cuerpo.isBlank()) {
                JsonNode nodo = LECTOR_JSON.readTree(cuerpo);
                if (tipo == null) {
                    tipo = primeroNoVacio(texto(nodo, "type"), texto(nodo, "topic"));
                }
                if (id == null) {
                    id = texto(nodo, "data", "id");
                }
            }
            if (id == null) {
                log.debug("Notificación de Mercado Pago sin id: {}", parametros);
                return false;
            }

            Optional<MercadoPagoQrCliente.EstadoConsulta> consulta =
                    (tipo != null && tipo.contains("merchant_order"))
                            ? mercadoPago.consultarMerchantOrder(id)
                            : mercadoPago.consultarPago(id);

            if (consulta.isEmpty()) {
                return false;
            }
            MercadoPagoQrCliente.EstadoConsulta estado = consulta.get();
            if ("approved".equalsIgnoreCase(estado.estado())) {
                return marcarAprobado(estado.referenciaExterna(), estado.idPago());
            }
            return false;
        } catch (Exception e) {
            log.warn("No se pudo procesar la notificación de Mercado Pago: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Acredita el pago de un pedido (idempotente) y marca la factura como PAGADA.
     *
     * @param idPedido   referencia externa enviada a Mercado Pago (id del pedido).
     * @param idPagoMp   id del pago de Mercado Pago.
     * @return true si el pago cambió a APROBADO en esta llamada.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean marcarAprobado(String idPedido, String idPagoMp) {
        if (idPedido == null) {
            return false;
        }
        Optional<Pago> encontrado = repositorioPago.findByPedido_IdAndEliminadoFalse(idPedido);
        if (encontrado.isEmpty()) {
            log.warn("Llegó una notificación de pago para un pedido sin pago registrado: {}", idPedido);
            return false;
        }
        Pago pago = encontrado.get();
        if (pago.getEstado() == EstadoPago.APROBADO) {
            return false;
        }
        pago.setEstado(EstadoPago.APROBADO);
        pago.setFechaAprobacion(LocalDateTime.now());
        if (idPagoMp != null && !idPagoMp.isBlank()) {
            pago.setIdPagoMp(idPagoMp);
        }
        repositorioPago.save(pago);

        servicioFacturaCliente.marcarPagada(idPedido).ifPresent(factura ->
                servicioCorreo.enviarPagoAprobado(pago.getPedido(), factura));
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<Pago> pagoDelPedido(String idPedido) {
        return repositorioPago.findByPedido_IdAndEliminadoFalse(idPedido);
    }

    /** Pagos de varios pedidos en una sola consulta (para los listados). */
    @Transactional(readOnly = true)
    public Map<String, Pago> pagosDePedidos(Collection<String> idsPedido) {
        if (idsPedido == null || idsPedido.isEmpty()) {
            return Map.of();
        }
        List<Pago> pagos = repositorioPago.findByPedido_IdInAndEliminadoFalse(idsPedido);
        return pagos.stream().collect(Collectors.toMap(p -> p.getPedido().getId(), Function.identity(), (a, b) -> a));
    }

    private static String primeroNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    private static String texto(JsonNode nodo, String... ruta) {
        JsonNode actual = nodo;
        for (String clave : ruta) {
            if (actual == null) {
                return null;
            }
            actual = actual.get(clave);
        }
        return actual == null || actual.isNull() ? null : actual.asText();
    }
}
