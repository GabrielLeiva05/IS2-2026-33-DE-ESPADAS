package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mercado Pago con <b>Checkout API</b>: al iniciar el cobro se genera un QR dinámico en la caja del
 * vendedor y se envía por correo al cliente; el pago con tarjeta se resuelve con el token del
 * navegador. Nunca se redirige al comprador a otra página. La acreditación se verifica siempre
 * server-to-server.
 */
@Component
public class EstrategiaPagoMercadoPago implements EstrategiaPago, EstrategiaNotificable, EstrategiaCobroTarjeta {

    private final ClienteMercadoPago cliente;
    private final Duration vigencia;

    public EstrategiaPagoMercadoPago(ClienteMercadoPago cliente,
                                     @Value("${app.pagos.mercadopago.vigencia-horas:24}") long vigenciaHoras) {
        this.cliente = cliente;
        this.vigencia = Duration.ofHours(vigenciaHoras);
    }

    @Override
    public MedioPago medio() {
        return MedioPago.MERCADO_PAGO;
    }

    @Override
    public DatosInicioPago iniciar(Pago pago) throws MiException {
        if (!cliente.estaConfigurado()) {
            throw new MiException("Mercado Pago no está configurado en el servidor: falta el access token");
        }
        ClienteMercadoPago.OrdenQr orden = cliente.crearOrdenQr(pago.getReferenciaOperacion(),
                pago.getImporte(), "Pedido Dulce Mayor " + pago.getPedido().getId());
        return DatosInicioPago.conQr(
                "Escaneá el QR con la app de Mercado Pago para pagar el total. "
                        + "La reserva se libera si el pago no se acredita a las 24 horas.",
                orden.idOrden(), orden.qrContenido());
    }

    @Override
    public Duration vigenciaReserva() {
        return vigencia;
    }

    @Override
    public void acreditar(Pago pago, String referencia, String observacion) throws MiException {
        if (referencia == null || referencia.isBlank()) {
            throw new MiException("Indicá el identificador del pago de Mercado Pago para verificarlo");
        }
        ClienteMercadoPago.PagoExterno externo = cliente.consultarPago(referencia);
        if (!externo.aprobado()) {
            throw new MiException("Mercado Pago informa el pago como '" + externo.estado() + "': no se acredita");
        }
        if (externo.referenciaOperacion() != null
                && !pago.getReferenciaOperacion().equals(externo.referenciaOperacion())) {
            throw new MiException("El pago de Mercado Pago no corresponde a este pedido");
        }
    }

    /** Cobro con tarjeta sin redirección: se crea el pago con el token del navegador. */
    @Override
    public ResultadoTarjeta cobrarConTarjeta(Pago pago, ClienteMercadoPago.DatosTarjeta tarjeta) throws MiException {
        if (tarjeta == null || tarjeta.token() == null || tarjeta.token().isBlank()) {
            throw new MiException("Faltan los datos de la tarjeta tokenizada");
        }
        if (tarjeta.paymentMethodId() == null || tarjeta.paymentMethodId().isBlank()) {
            throw new MiException("Falta el medio de pago de la tarjeta");
        }
        ClienteMercadoPago.PagoExterno externo = cliente.crearPagoConTarjeta(pago.getReferenciaOperacion(),
                pago.getImporte(), tarjeta, "Pedido Dulce Mayor " + pago.getPedido().getId());
        return new ResultadoTarjeta(externo.id(), externo.estado(), externo.aprobado());
    }

    @Override
    public NotificacionPago resolver(String firma, String requestId, String dataId, String tipo) throws MiException {
        if (!cliente.webhookConfigurado()) {
            throw new MiException("El webhook de Mercado Pago no está configurado en el servidor");
        }
        if (!cliente.firmaValida(firma, requestId, dataId)) {
            throw new MiException("La firma de la notificación de Mercado Pago es inválida");
        }
        if (tipo == null || !"payment".equalsIgnoreCase(tipo)) {
            return null;
        }
        ClienteMercadoPago.PagoExterno externo = cliente.consultarPago(dataId);
        return new NotificacionPago(externo.referenciaOperacion(), externo.aprobado(), externo.id());
    }
}
