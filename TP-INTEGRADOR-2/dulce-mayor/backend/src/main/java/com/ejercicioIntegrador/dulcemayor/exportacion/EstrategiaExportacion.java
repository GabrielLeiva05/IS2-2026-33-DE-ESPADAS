package com.ejercicioIntegrador.dulcemayor.exportacion;

/**
 * PATRÓN STRATEGY — ESTRATEGIA: interfaz común de todas las exportaciones.
 *
 * Cada implementación conoce UN formato (formato()) para UN tipo de reporte (tipoDeReporte()) y sabe
 * convertir el reporte ya armado por el servicio en los bytes del archivo. Una estrategia no consulta la base
 * ni aplica reglas de negocio: solo da formato a los datos que recibe.
 * Para agregar un formato nuevo (por ejemplo CSV) alcanza con crear otra clase @Component que implemente esta
 * interfaz; ContextoExportacion la descubre sola y no hay que tocar ni el servicio ni el controlador.
 *
 * @param <R> tipo de reporte (DTO) que exporta la estrategia
 */
public interface EstrategiaExportacion<R> {

    /** Tipo de reporte que sabe exportar esta estrategia. */
    Class<R> tipoDeReporte();

    /** Formato del archivo que genera. */
    FormatoExportacion formato();

    /** Genera el archivo. Ante una falla técnica lanza ErrorExportacionException. */
    byte[] exportar(R reporte);
}
