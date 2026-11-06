package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mercado Pago (Checkout Pro): crea la preferencia y expone el enlace/QR. La acreditación se
 * verifica siempre server-to-server; nunca se confía en el retorno del navegador.
 */
@Component
public class EstrategiaPagoMercadoPago implements EstrategiaPago, EstrategiaNotificable {

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
        ClienteMercadoPago.Preferencia preferencia = cliente.crearPreferencia(pago.getReferenciaOperacion(),
                pago.getImporte(), "Pedido Dulce Mayor " + pago.getPedido().getId());
        return new DatosInicioPago(
                "Pagá el total con el enlace o el QR de Mercado Pago. La reserva se libera si no se acredita a las 24 horas.",
                preferencia.enlacePago(), preferencia.id());
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
