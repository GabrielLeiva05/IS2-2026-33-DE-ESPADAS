package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Pago;

import java.time.Duration;

/**
 * Operaciones realmente compartidas por los medios de pago (Strategy, plan sección 3): iniciar el
 * cobro, declarar la vigencia de la reserva y registrar la acreditación. Cada proveedor decide
 * cómo lo hace; las operaciones específicas (webhook, QR) viven en {@link EstrategiaNotificable}.
 */
public interface EstrategiaPago {

    MedioPago medio();

    DatosInicioPago iniciar(Pago pago) throws MiException;

    /** Tiempo máximo que la reserva de stock se mantiene esperando el pago. */
    Duration vigenciaReserva();

    /**
     * Verifica lo que corresponda y deja constancia de la acreditación. El cambio de estado del
     * pago lo aplica el servicio; acá se valida el dato del proveedor.
     */
    void acreditar(Pago pago, String referencia, String observacion) throws MiException;
}
