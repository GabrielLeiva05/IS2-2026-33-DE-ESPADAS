package com.ejercicioIntegrador.dulcemayor.exportacion;

import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PATRÓN STRATEGY — CONTEXTO: elige y ejecuta la estrategia de exportación.
 *
 * Spring le inyecta todas las EstrategiaExportacion registradas. La elección se hace por polimorfismo, según el
 * tipo de reporte recibido y el formato pedido: no hay if/else ni switch por formato, y la selección vive solo
 * aquí (ni en los controladores ni en los servicios). Si no hay estrategia para la combinación pedida
 * (por ejemplo, el histórico de ventas en PDF) responde con un error de negocio claro.
 */
@Component
public class ContextoExportacion {

    private final List<EstrategiaExportacion<?>> estrategias;

    public ContextoExportacion(List<EstrategiaExportacion<?>> estrategias) {
        this.estrategias = List.copyOf(estrategias);
    }

    public byte[] exportar(Object reporte, FormatoExportacion formato) throws MiException {
        if (reporte == null) {
            throw new MiException("No hay un reporte para exportar");
        }
        if (formato == null) {
            throw new MiException("Debe indicar el formato de exportación");
        }
        for (EstrategiaExportacion<?> estrategia : estrategias) {
            if (estrategia.formato() == formato && estrategia.tipoDeReporte().isInstance(reporte)) {
                return ejecutar(estrategia, reporte);
            }
        }
        throw new MiException("Este reporte no se puede exportar en formato " + formato);
    }

    /** Captura el tipo de la estrategia para invocarla sin conversiones sin verificar. */
    private static <R> byte[] ejecutar(EstrategiaExportacion<R> estrategia, Object reporte) {
        return estrategia.exportar(estrategia.tipoDeReporte().cast(reporte));
    }
}
