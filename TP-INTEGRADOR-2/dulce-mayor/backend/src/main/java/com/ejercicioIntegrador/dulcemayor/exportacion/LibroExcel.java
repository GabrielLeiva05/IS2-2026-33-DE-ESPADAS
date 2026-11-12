package com.ejercicioIntegrador.dulcemayor.exportacion;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Herramientas de Apache POI compartidas por las estrategias de Excel: estilos con la identidad visual de
 * Dulce Mayor (rosa de los encabezados del PDF y del panel) y escritura de celdas. No conoce ningún reporte.
 */
final class LibroExcel implements AutoCloseable {

    private static final String FORMATO_MONEDA = "\"$\" #,##0.00";

    final XSSFWorkbook libro = new XSSFWorkbook();
    final CellStyle titulo;
    final CellStyle subtitulo;
    final CellStyle encabezado;
    final CellStyle texto;
    final CellStyle entero;
    final CellStyle moneda;
    final CellStyle fecha;
    final CellStyle fechaHora;
    final CellStyle etiquetaTotal;
    final CellStyle monedaTotal;
    final CellStyle enteroTotal;
    final CellStyle seccion;

    LibroExcel() {
        CreationHelper ayudante = libro.getCreationHelper();
        short formatoMoneda = libro.createDataFormat().getFormat(FORMATO_MONEDA);

        XSSFFont negritaGrande = libro.createFont();
        negritaGrande.setBold(true);
        negritaGrande.setFontHeightInPoints((short) 14);
        negritaGrande.setColor(rosaOscuro());
        titulo = libro.createCellStyle();
        titulo.setFont(negritaGrande);

        Font gris = libro.createFont();
        gris.setItalic(true);
        subtitulo = libro.createCellStyle();
        subtitulo.setFont(gris);

        Font blanca = libro.createFont();
        blanca.setBold(true);
        blanca.setColor(IndexedColors.WHITE.getIndex());
        XSSFCellStyle estiloEncabezado = libro.createCellStyle();
        estiloEncabezado.setFont(blanca);
        estiloEncabezado.setFillForegroundColor(rosaOscuro());
        estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
        estiloEncabezado.setWrapText(true);
        encabezado = estiloEncabezado;

        texto = celda();
        entero = celda();
        entero.setDataFormat(libro.createDataFormat().getFormat("#,##0"));
        moneda = celda();
        moneda.setDataFormat(formatoMoneda);
        fecha = celda();
        fecha.setDataFormat(ayudante.createDataFormat().getFormat("dd/mm/yyyy"));
        fecha.setAlignment(HorizontalAlignment.CENTER);
        fechaHora = celda();
        fechaHora.setDataFormat(ayudante.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        fechaHora.setAlignment(HorizontalAlignment.CENTER);

        Font negrita = libro.createFont();
        negrita.setBold(true);
        XSSFCellStyle estiloTotal = libro.createCellStyle();
        estiloTotal.setFont(negrita);
        estiloTotal.setFillForegroundColor(rosaClaro());
        estiloTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        etiquetaTotal = estiloTotal;
        monedaTotal = libro.createCellStyle();
        monedaTotal.cloneStyleFrom(etiquetaTotal);
        monedaTotal.setDataFormat(formatoMoneda);
        enteroTotal = libro.createCellStyle();
        enteroTotal.cloneStyleFrom(etiquetaTotal);
        enteroTotal.setDataFormat(libro.createDataFormat().getFormat("#,##0"));

        Font seccionFuente = libro.createFont();
        seccionFuente.setBold(true);
        seccionFuente.setFontHeightInPoints((short) 12);
        seccion = libro.createCellStyle();
        seccion.setFont(seccionFuente);
    }

    private CellStyle celda() {
        CellStyle estilo = libro.createCellStyle();
        estilo.setBorderBottom(BorderStyle.HAIR);
        return estilo;
    }

    private static XSSFColor rosaOscuro() {
        return new XSSFColor(new byte[]{(byte) 0xD6, (byte) 0x33, (byte) 0x6C}, null);
    }

    private static XSSFColor rosaClaro() {
        return new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xB6, (byte) 0xC1}, null);
    }

    /** Escribe una fila de encabezados de columna. */
    void escribirEncabezados(Sheet hoja, int numeroFila, String... titulos) {
        Row fila = hoja.createRow(numeroFila);
        fila.setHeightInPoints(28);
        for (int i = 0; i < titulos.length; i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(titulos[i]);
            celda.setCellStyle(encabezado);
        }
    }

    void texto(Row fila, int columna, String valor) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? "" : valor);
        celda.setCellStyle(texto);
    }

    void entero(Row fila, int columna, long valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    void moneda(Row fila, int columna, Double valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor == null) {
            celda.setBlank();
        } else {
            celda.setCellValue(valor);
        }
        celda.setCellStyle(estilo);
    }

    void fechaHora(Row fila, int columna, LocalDateTime valor) {
        Cell celda = fila.createCell(columna);
        if (valor == null) {
            celda.setBlank();
        } else {
            celda.setCellValue(valor);
        }
        celda.setCellStyle(fechaHora);
    }

    void fecha(Row fila, int columna, LocalDateTime valor) {
        Cell celda = fila.createCell(columna);
        if (valor == null) {
            celda.setBlank();
        } else {
            celda.setCellValue(valor.toLocalDate());
        }
        celda.setCellStyle(fecha);
    }

    void celdaConEstilo(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? "" : valor);
        celda.setCellStyle(estilo);
    }

    /** Anchos de columna fijos (en caracteres): no se usa autoSizeColumn para no depender de las fuentes del servidor. */
    void anchos(Sheet hoja, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) {
            hoja.setColumnWidth(i, Math.min(caracteres[i], 80) * 256);
        }
    }

    byte[] aBytes() throws IOException {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    @Override
    public void close() throws IOException {
        libro.close();
    }
}
