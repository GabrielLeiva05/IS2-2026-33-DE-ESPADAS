package com.ejercicioIntegrador.dulcemayor.exportacion;

import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Patrón Strategy: el contexto elige la estrategia por tipo de reporte y formato, sin condicionales por formato. */
class ExportadorReportesTest {

    /** Estrategia de prueba para el reporte de ventas en PDF (en producción solo existe en Excel). */
    private static EstrategiaExportacion<ReporteVentasDTO> ventasEn(FormatoExportacion formato, byte[] resultado) {
        return new EstrategiaExportacion<>() {
            @Override
            public Class<ReporteVentasDTO> tipoDeReporte() {
                return ReporteVentasDTO.class;
            }

            @Override
            public FormatoExportacion formato() {
                return formato;
            }

            @Override
            public byte[] exportar(ReporteVentasDTO reporte) {
                return resultado;
            }
        };
    }

    private static ReporteVentasDTO reporteVacio() {
        return new ReporteVentasDTO(null, null, null, null, null, null, List.of(), List.of(), List.of(), List.of(),
                0, 0, 0);
    }

    @Test
    void ejecutaLaEstrategiaDelFormatoPedido() throws MiException {
        byte[] pdf = {1};
        byte[] excel = {2};
        ExportadorReportes exportador = new ExportadorReportes(
                List.of(ventasEn(FormatoExportacion.PDF, pdf), ventasEn(FormatoExportacion.EXCEL, excel)));

        assertArrayEquals(pdf, exportador.exportar(reporteVacio(), FormatoExportacion.PDF));
        assertArrayEquals(excel, exportador.exportar(reporteVacio(), FormatoExportacion.EXCEL));
    }

    @Test
    void rechazaUnaCombinacionSinEstrategia() {
        ExportadorReportes exportador = new ExportadorReportes(List.of(ventasEn(FormatoExportacion.EXCEL, new byte[0])));

        assertThrows(MiException.class, () -> exportador.exportar(reporteVacio(), FormatoExportacion.PDF));
        // Una estrategia de ventas no sirve para otro tipo de reporte
        ReporteClientesPedidosDTO otro = new ReporteClientesPedidosDTO(null, null, null, null, null, null, null,
                List.of(), 0, 0, 0, 0, 0);
        assertThrows(MiException.class, () -> exportador.exportar(otro, FormatoExportacion.EXCEL));
    }

    @Test
    void rechazaReporteOFormatoNulos() {
        ExportadorReportes exportador = new ExportadorReportes(List.of());
        assertThrows(MiException.class, () -> exportador.exportar(null, FormatoExportacion.PDF));
        assertThrows(MiException.class, () -> exportador.exportar(reporteVacio(), null));
    }
}
