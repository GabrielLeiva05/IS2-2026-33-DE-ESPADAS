package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cobro en efectivo: se registra recién al entregar. Mientras que la entrega exista en la Fase 5,
 * el administrativo lo registra a mano (alcance aprobado). La reserva se sostiene hasta la ventana
 * configurada (7 días por defecto).
 */
@Component
public class EstrategiaPagoEfectivo implements EstrategiaPago {

    private final String instrucciones;
    private final Duration vigencia;

    public EstrategiaPagoEfectivo(
            @Value("${app.pagos.efectivo.instrucciones:El cobro en efectivo se registra al entregar el pedido.}")
            String instrucciones,
            @Value("${app.pagos.efectivo.vigencia-horas:168}") long vigenciaHoras) {
        this.instrucciones = instrucciones;
        this.vigencia = Duration.ofHours(vigenciaHoras);
    }

    @Override
    public MedioPago medio() {
        return MedioPago.EFECTIVO;
    }

    @Override
    public DatosInicioPago iniciar(Pago pago) {
        return DatosInicioPago.manual(instrucciones);
    }

    @Override
    public Duration vigenciaReserva() {
        return vigencia;
    }

    @Override
    public void acreditar(Pago pago, String referencia, String observacion) {
        // El efectivo no requiere referencia externa: el administrativo (o el distribuidor en la
        // Fase 5) confirma que lo recibió.
    }
}
