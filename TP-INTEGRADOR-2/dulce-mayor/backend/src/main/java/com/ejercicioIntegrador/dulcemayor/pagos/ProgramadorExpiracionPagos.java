package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tarea programada de vencimiento: a las 24 h (transferencia / Mercado Pago) el pedido impago se
 * cancela y la reserva se libera. Cada pago se expira en su propia transacción, así un registro
 * problemático no bloquea a los demás.
 */
@Component
@ConditionalOnProperty(name = "app.pagos.expiracion-habilitada", havingValue = "true", matchIfMissing = true)
public class ProgramadorExpiracionPagos {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramadorExpiracionPagos.class);

    private final ServicioPago servicioPago;

    public ProgramadorExpiracionPagos(ServicioPago servicioPago) {
        this.servicioPago = servicioPago;
    }

    @Scheduled(fixedDelayString = "${app.pagos.expiracion-intervalo-ms:60000}",
            initialDelayString = "${app.pagos.expiracion-retraso-inicial-ms:60000}")
    public void expirarPagosVencidos() {
        List<String> vencidos = servicioPago.pagosVencidos();
        for (String idPago : vencidos) {
            try {
                servicioPago.expirar(idPago);
            } catch (RuntimeException e) {
                LOG.warn("No se pudo expirar el pago {}", idPago, e);
            }
        }
    }
}
