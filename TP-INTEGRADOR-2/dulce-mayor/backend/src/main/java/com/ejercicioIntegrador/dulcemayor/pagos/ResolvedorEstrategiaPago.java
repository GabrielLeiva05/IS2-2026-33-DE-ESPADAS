package com.ejercicioIntegrador.dulcemayor.pagos;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Selecciona la estrategia según el medio del pedido (fábrica del patrón Strategy). */
@Component
public class ResolvedorEstrategiaPago {

    private final Map<MedioPago, EstrategiaPago> estrategias = new EnumMap<>(MedioPago.class);

    public ResolvedorEstrategiaPago(List<EstrategiaPago> disponibles) {
        disponibles.forEach(estrategia -> estrategias.put(estrategia.medio(), estrategia));
    }

    public EstrategiaPago obtener(MedioPago medio) throws MiException {
        EstrategiaPago estrategia = medio == null ? null : estrategias.get(medio);
        if (estrategia == null) {
            throw new MiException("El medio de pago indicado no está habilitado");
        }
        return estrategia;
    }

    public EstrategiaNotificable notificable(MedioPago medio) throws MiException {
        EstrategiaPago estrategia = obtener(medio);
        if (!(estrategia instanceof EstrategiaNotificable notificable)) {
            throw new MiException("El medio de pago no admite notificaciones del proveedor");
        }
        return notificable;
    }

    public boolean admiteNotificaciones(MedioPago medio) {
        EstrategiaPago estrategia = medio == null ? null : estrategias.get(medio);
        return estrategia instanceof EstrategiaNotificable;
    }
}
