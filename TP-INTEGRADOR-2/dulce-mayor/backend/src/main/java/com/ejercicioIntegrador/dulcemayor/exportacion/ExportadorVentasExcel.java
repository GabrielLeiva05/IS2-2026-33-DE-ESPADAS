package com.ejercicioIntegrador.dulcemayor.exportacion;

import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubtotalVentasDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VentaDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Estrategia concreta: histórico de ventas en Excel (.xlsx), con Apache POI.
 * Hoja "Resumen": totales generales y subtotales por departamento, por preventista y por departamento y preventista.
 * Hoja "Detalle": una fila por venta, ordenada por departamento, preventista y fecha (el orden lo arma el servicio).
 */
@Component
public class ExportadorVentasExcel implements EstrategiaExportacion<ReporteVentasDTO> {

    private static final String[] COLUMNAS_DETALLE = {"Departamento", "Preventista", "Documento del cliente",
            "Cliente", "Comercio", "N.º pedido", "Fecha de venta", "Subtotal", "Descuento", "Total"};
    private static final int FILA_ENCABEZADO_DETALLE = 5;

    @Override
    public Class<ReporteVentasDTO> tipoDeReporte() {
        return ReporteVentasDTO.class;
    }

    @Override
    public FormatoExportacion formato() {
        return FormatoExportacion.EXCEL;
    }

    @Override
    public byte[] exportar(ReporteVentasDTO reporte) {
        try (LibroExcel excel = new LibroExcel()) {
            escribirResumen(excel, excel.libro.createSheet("Resumen"), reporte);
            escribirDetalle(excel, excel.libro.createSheet("Detalle"), reporte);
            return excel.aBytes();
        } catch (IOException e) {
            throw new ErrorExportacionException("No se pudo generar el Excel del histórico de ventas", e);
        }
    }

    // ------------------------------------------------------------------ Resumen

    private void escribirResumen(LibroExcel excel, Sheet hoja, ReporteVentasDTO reporte) {
        escribirCabecera(excel, hoja, reporte);

        Row fila = hoja.createRow(4);
        excel.celdaConEstilo(fila, 0, "TOTAL GENERAL DEL PERÍODO", excel.etiquetaTotal);
        excel.celdaConEstilo(fila, 1, "", excel.etiquetaTotal);
        excel.entero(fila, 2, reporte.cantidadPedidos(), excel.enteroTotal);
        excel.moneda(fila, 3, reporte.totalGeneral(), excel.monedaTotal);
        Row clientes = hoja.createRow(5);
        excel.texto(clientes, 0, "Clientes distintos que compraron");
        excel.entero(clientes, 2, reporte.cantidadClientes(), excel.entero);

        int siguiente = 7;
        siguiente = bloque(excel, hoja, siguiente, "Ventas por departamento", reporte.porDepartamento(), true, false);
        siguiente = bloque(excel, hoja, siguiente, "Ventas por preventista", reporte.porPreventista(), false, true);
        bloque(excel, hoja, siguiente, "Ventas por departamento y preventista", reporte.porDepartamentoYPreventista(),
                true, true);
        excel.anchos(hoja, 34, 30, 12, 18);
    }

    private void escribirCabecera(LibroExcel excel, Sheet hoja, ReporteVentasDTO reporte) {
        excel.celdaConEstilo(hoja.createRow(0), 0, "Dulce Mayor - Histórico de ventas", excel.titulo);
        excel.celdaConEstilo(hoja.createRow(1), 0, "Período: " + TextoReporte.periodo(reporte.desde(), reporte.hasta()),
                excel.subtitulo);
        excel.celdaConEstilo(hoja.createRow(2), 0, TextoReporte.filtros("Departamento", reporte.departamentoFiltrado(),
                "Preventista", reporte.preventistaFiltrado()), excel.subtitulo);
        excel.celdaConEstilo(hoja.createRow(3), 0, "Generado por " + (reporte.generadoPor() == null ? "-" : reporte.generadoPor())
                + " el " + TextoReporte.fechaHora(reporte.generadoEn()), excel.subtitulo);
    }

    /** Escribe un bloque de subtotales y devuelve la fila donde puede empezar el siguiente. */
    private int bloque(LibroExcel excel, Sheet hoja, int filaInicial, String titulo, List<SubtotalVentasDTO> subtotales,
                       boolean conDepartamento, boolean conPreventista) {
        excel.celdaConEstilo(hoja.createRow(filaInicial), 0, titulo, excel.seccion);
        excel.escribirEncabezados(hoja, filaInicial + 1, "Departamento", "Preventista", "Pedidos", "Total vendido");
        int numeroFila = filaInicial + 2;
        for (SubtotalVentasDTO subtotal : subtotales) {
            Row fila = hoja.createRow(numeroFila++);
            excel.texto(fila, 0, conDepartamento ? subtotal.departamento() : "");
            excel.texto(fila, 1, conPreventista ? subtotal.preventista() : "");
            excel.entero(fila, 2, subtotal.cantidadPedidos(), excel.entero);
            excel.moneda(fila, 3, subtotal.total(), excel.moneda);
        }
        if (subtotales.isEmpty()) {
            excel.texto(hoja.createRow(numeroFila++), 0, "Sin ventas en el período");
        }
        return numeroFila + 1;
    }

    // ------------------------------------------------------------------ Detalle

    private void escribirDetalle(LibroExcel excel, Sheet hoja, ReporteVentasDTO reporte) {
        escribirCabecera(excel, hoja, reporte);
        excel.escribirEncabezados(hoja, FILA_ENCABEZADO_DETALLE, COLUMNAS_DETALLE);

        int numeroFila = FILA_ENCABEZADO_DETALLE + 1;
        for (VentaDTO venta : reporte.ventas()) {
            Row fila = hoja.createRow(numeroFila++);
            excel.texto(fila, 0, venta.departamento());
            excel.texto(fila, 1, venta.preventista());
            excel.texto(fila, 2, venta.numeroDocumentoCliente());
            excel.texto(fila, 3, venta.nombreCliente());
            excel.texto(fila, 4, venta.nombreComercio());
            excel.entero(fila, 5, venta.numeroPedido(), excel.texto);
            excel.fechaHora(fila, 6, venta.fechaVenta());
            excel.moneda(fila, 7, venta.subtotal(), excel.moneda);
            excel.moneda(fila, 8, venta.descuento(), excel.moneda);
            excel.moneda(fila, 9, venta.total(), excel.moneda);
        }

        Row total = hoja.createRow(numeroFila + 1);
        excel.celdaConEstilo(total, 0, "TOTAL GENERAL (" + reporte.cantidadPedidos() + " pedidos)", excel.etiquetaTotal);
        for (int columna = 1; columna < 9; columna++) {
            excel.celdaConEstilo(total, columna, "", excel.etiquetaTotal);
        }
        excel.moneda(total, 9, reporte.totalGeneral(), excel.monedaTotal);

        hoja.createFreezePane(0, FILA_ENCABEZADO_DETALLE + 1);
        if (!reporte.ventas().isEmpty()) {
            hoja.setAutoFilter(new CellRangeAddress(FILA_ENCABEZADO_DETALLE, numeroFila - 1, 0, COLUMNAS_DETALLE.length - 1));
        }
        excel.anchos(hoja, 24, 26, 18, 28, 28, 11, 17, 16, 16, 16);
    }
}
