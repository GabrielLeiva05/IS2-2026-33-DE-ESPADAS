package com.ejercicioIntegrador.dulcemayor.exportacion;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Estrategia concreta: reporte de clientes y pedidos en Excel (.xlsx), con Apache POI.
 * Una fila por pedido con los datos del cliente y del comercio, y una fila final de totales.
 */
@Component
public class ExportadorClientesPedidosExcel implements EstrategiaExportacion<ReporteClientesPedidosDTO> {

    private static final String[] COLUMNAS = {"Documento", "Cliente", "Correo", "Comercio", "Departamento",
            "Domicilio del comercio", "N.º pedido", "Fecha de carga", "Preventista", "Estado del pedido",
            "Estado de envío", "Distribuidor", "Fecha de facturación", "Fecha de entrega", "Monto facturado"};
    private static final int FILA_ENCABEZADO = 5;
    private static final int COLUMNA_MONTO = 14;

    @Override
    public Class<ReporteClientesPedidosDTO> tipoDeReporte() {
        return ReporteClientesPedidosDTO.class;
    }

    @Override
    public FormatoExportacion formato() {
        return FormatoExportacion.EXCEL;
    }

    @Override
    public byte[] exportar(ReporteClientesPedidosDTO reporte) {
        try (LibroExcel excel = new LibroExcel()) {
            Sheet hoja = excel.libro.createSheet("Clientes y pedidos");
            escribirCabecera(excel, hoja, reporte);
            excel.escribirEncabezados(hoja, FILA_ENCABEZADO, COLUMNAS);

            int numeroFila = FILA_ENCABEZADO + 1;
            for (PedidoReporteDTO pedido : reporte.pedidos()) {
                escribirPedido(excel, hoja.createRow(numeroFila++), pedido);
            }
            escribirTotales(excel, hoja, numeroFila, reporte);

            hoja.createFreezePane(0, FILA_ENCABEZADO + 1);
            if (!reporte.pedidos().isEmpty()) {
                hoja.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO, numeroFila - 1, 0, COLUMNAS.length - 1));
            }
            excel.anchos(hoja, 14, 26, 30, 28, 18, 34, 11, 17, 24, 18, 22, 24, 18, 17, 16);
            return excel.aBytes();
        } catch (IOException e) {
            throw new ErrorExportacionException("No se pudo generar el Excel de clientes y pedidos", e);
        }
    }

    private void escribirCabecera(LibroExcel excel, Sheet hoja, ReporteClientesPedidosDTO reporte) {
        excel.celdaConEstilo(hoja.createRow(0), 0, "Dulce Mayor - Reporte de clientes y pedidos", excel.titulo);
        excel.celdaConEstilo(hoja.createRow(1), 0, "Período: " + TextoReporte.periodo(reporte.desde(), reporte.hasta()),
                excel.subtitulo);
        excel.celdaConEstilo(hoja.createRow(2), 0, TextoReporte.filtros("Departamento", reporte.departamentoFiltrado(),
                "Estado", reporte.descripcionEstadoFiltrado()), excel.subtitulo);
        excel.celdaConEstilo(hoja.createRow(3), 0, "Generado por " + (reporte.generadoPor() == null ? "-" : reporte.generadoPor())
                + " el " + TextoReporte.fechaHora(reporte.generadoEn()), excel.subtitulo);
    }

    private void escribirPedido(LibroExcel excel, Row fila, PedidoReporteDTO p) {
        excel.texto(fila, 0, p.numeroDocumentoCliente());
        excel.texto(fila, 1, p.nombreCliente());
        excel.texto(fila, 2, p.correoCliente());
        excel.texto(fila, 3, p.nombreComercio());
        excel.texto(fila, 4, p.departamento());
        excel.texto(fila, 5, p.domicilioComercio());
        excel.entero(fila, 6, p.numeroPedido(), excel.texto);
        excel.fechaHora(fila, 7, p.fechaHoraCarga());
        excel.texto(fila, 8, p.preventista());
        excel.texto(fila, 9, p.descripcionEstado());
        excel.texto(fila, 10, p.estadoEnvio());
        excel.texto(fila, 11, p.distribuidor());
        excel.fecha(fila, 12, p.fechaFacturacion());
        excel.fechaHora(fila, 13, p.fechaEntrega());
        excel.moneda(fila, COLUMNA_MONTO, p.montoFacturado(), excel.moneda);
    }

    private void escribirTotales(LibroExcel excel, Sheet hoja, int numeroFila, ReporteClientesPedidosDTO reporte) {
        Row fila = hoja.createRow(numeroFila + 1);
        excel.celdaConEstilo(fila, 0, "TOTALES", excel.etiquetaTotal);
        excel.celdaConEstilo(fila, 1, "Comercios: " + reporte.cantidadComercios(), excel.etiquetaTotal);
        excel.celdaConEstilo(fila, 2, "Pedidos: " + reporte.cantidadPedidos(), excel.etiquetaTotal);
        excel.celdaConEstilo(fila, 3, "Entregados: " + reporte.cantidadEntregados(), excel.etiquetaTotal);
        excel.celdaConEstilo(fila, 4, "Pend. de entrega: " + reporte.cantidadPendientesDeEntrega(), excel.etiquetaTotal);
        for (int columna = 5; columna < COLUMNA_MONTO; columna++) {
            excel.celdaConEstilo(fila, columna, "", excel.etiquetaTotal);
        }
        excel.moneda(fila, COLUMNA_MONTO, reporte.totalFacturado(), excel.monedaTotal);
    }
}
