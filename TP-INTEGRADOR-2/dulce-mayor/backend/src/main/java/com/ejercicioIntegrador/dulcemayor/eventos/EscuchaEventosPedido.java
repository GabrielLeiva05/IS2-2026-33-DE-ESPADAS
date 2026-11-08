package com.ejercicioIntegrador.dulcemayor.eventos;

import com.ejercicioIntegrador.dulcemayor.service.ServicioCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador de los eventos del pedido: arma el correo y lo entrega al {@link ServicioCorreo}.
 * Nunca propaga el fallo de envío a la transacción del pedido: si el correo no sale queda registrado
 * como {@code FALLIDO} y la operación comercial sigue siendo válida.
 */
@Component
public class EscuchaEventosPedido {

    private static final Logger LOG = LoggerFactory.getLogger(EscuchaEventosPedido.class);

    private final ServicioCorreo servicioCorreo;

    public EscuchaEventosPedido(ServicioCorreo servicioCorreo) {
        this.servicioCorreo = servicioCorreo;
    }

    @EventListener
    public void alOcurrirEventoDePedido(EventoPedido evento) {
        if (evento.destinatario() == null || evento.destinatario().isBlank()) {
            LOG.debug("Evento {} del pedido {} sin destinatario: no se envía correo",
                    evento.tipo(), evento.idPedido());
            return;
        }
        try {
            servicioCorreo.enviar(evento.destinatario(), asunto(evento), cuerpo(evento), evento.tipo());
        } catch (RuntimeException e) {
            LOG.warn("No se pudo registrar el correo del pedido {}: {}", evento.idPedido(), e.getMessage());
        }
    }

    private static String comercio(EventoPedido evento) {
        return evento.nombreComercio() == null ? "tu comercio" : evento.nombreComercio();
    }

    private static String asunto(EventoPedido evento) {
        return switch (evento.tipo()) {
            case PEDIDO_CONFIRMADO -> "Pedido confirmado — " + comercio(evento);
            case PAGO_ACREDITADO -> "Pago acreditado — " + comercio(evento);
            case PEDIDO_CANCELADO -> "Pedido cancelado — " + comercio(evento);
            case ENTREGA_REGISTRADA -> "Entrega registrada — " + comercio(evento);
            case PROXIMA_ENTREGA -> "Tu pedido está en camino — " + comercio(evento);
            default -> "Dulce Mayor — " + comercio(evento);
        };
    }

    private static String cuerpo(EventoPedido evento) {
        String comercio = comercio(evento);
        String detalle = evento.detalle() == null ? "" : "\n\n" + evento.detalle();
        return switch (evento.tipo()) {
            case PEDIDO_CONFIRMADO -> """
                    Hola:

                    Confirmamos el pedido %s de %s y reservamos el stock correspondiente.
                    Cuando acreditemos el pago vas a poder descargar el comprobante interno desde el sitio.%s

                    Dulce Mayor
                    """.formatted(evento.idPedido(), comercio, detalle);
            case PAGO_ACREDITADO -> """
                    Hola:

                    Acreditamos el pago total del pedido %s de %s.
                    El comprobante interno no fiscal ya está disponible para descargar desde el sitio.%s

                    Dulce Mayor
                    """.formatted(evento.idPedido(), comercio, detalle);
            case PEDIDO_CANCELADO -> """
                    Hola:

                    El pedido %s de %s fue cancelado y la reserva de stock quedó liberada.%s

                    Dulce Mayor
                    """.formatted(evento.idPedido(), comercio, detalle);
            case ENTREGA_REGISTRADA -> """
                    Hola:

                    Registramos la entrega del pedido %s de %s.%s

                    Dulce Mayor
                    """.formatted(evento.idPedido(), comercio, detalle);
            case PROXIMA_ENTREGA -> """
                    Hola:

                    El pedido %s de %s está en la hoja de ruta de reparto de hoy.%s

                    Dulce Mayor
                    """.formatted(evento.idPedido(), comercio, detalle);
            default -> "Novedades del pedido " + evento.idPedido() + " de " + comercio + detalle;
        };
    }
}
