package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Transferencia bancaria conciliada a mano por el administrativo: no tiene QR ni webhook.
 * La reserva se libera si el pago no se acredita dentro de la ventana configurada (24 h por defecto).
 */
@Component
public class EstrategiaPagoTransferencia implements EstrategiaPago {

    private final String instrucciones;
    private final Duration vigencia;

    public EstrategiaPagoTransferencia(
            @Value("${app.pagos.transferencia.instrucciones:Transferí el importe total a la cuenta de Dulce Mayor y avisá el número de operación. La reserva se libera si no se acredita a las 24 horas.}")
            String instrucciones,
            @Value("${app.pagos.transferencia.vigencia-horas:24}") long vigenciaHoras) {
        this.instrucciones = instrucciones;
        this.vigencia = Duration.ofHours(vigenciaHoras);
    }

    @Override
    public MedioPago medio() {
        return MedioPago.TRANSFERENCIA;
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
    public void acreditar(Pago pago, String referencia, String observacion) throws MiException {
        if (referencia == null || referencia.isBlank()) {
            throw new MiException("Indicá el número de operación de la transferencia");
        }
    }
}
