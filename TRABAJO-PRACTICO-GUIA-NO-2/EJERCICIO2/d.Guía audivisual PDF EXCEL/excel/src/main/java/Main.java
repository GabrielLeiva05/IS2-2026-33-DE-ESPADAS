import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. Simulación de datos (lo que en Spring vendría del service/repositorio o del model)
        List<Cliente> listaC = new ArrayList<>();
        listaC.add(new Cliente(1L, "Juan", "Pérez", "123456789", "juan.perez@email.com", new Ciudad("Mendoza")));
        listaC.add(new Cliente(2L, "María", "Gómez", "987654321", "maria.gomez@email.com", new Ciudad("Córdoba")));
        listaC.add(new Cliente(3L, "Carlos", "López", "555123456", "carlos.lopez@email.com", new Ciudad("Buenos Aires")));

        String rutaArchivo = "listado-clientes.xlsx";

        // 2. Generación del reporte Excel con Apache POI
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(rutaArchivo)) {

            Sheet hoja = workbook.createSheet("Clientes");

            // Fila del título (fila 0)
            Row filaTitulo = hoja.createRow(0);
            Cell celda = filaTitulo.createCell(0);
            celda.setCellValue("LISTADO GENERAL DE CLIENTES");

            // Fila de encabezados de columnas (fila 2)
            Row filaData = hoja.createRow(2);
            String[] columnas = {"ID", "NOMBRES", "APELLIDOS", "TELEFONO", "CORREO", "CIUDAD"};

            for (int i = 0; i < columnas.length; i++) {
                celda = filaData.createCell(i);
                celda.setCellValue(columnas[i]);
            }

            // Filas con los datos de los clientes (a partir de la fila 3)
            int numFila = 3;
            for (Cliente cliente : listaC) {
                filaData = hoja.createRow(numFila);

                filaData.createCell(0).setCellValue(cliente.getId());
                filaData.createCell(1).setCellValue(cliente.getNombres());
                filaData.createCell(2).setCellValue(cliente.getApellidos());
                filaData.createCell(3).setCellValue(cliente.getTelefono());
                filaData.createCell(4).setCellValue(cliente.getEmail());
                filaData.createCell(5).setCellValue(cliente.getCiudad().getCiudad());

                numFila++;
            }

            // Opcional: autoajustar el ancho de las columnas al contenido
            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            // Escribir el archivo en disco
            workbook.write(outputStream);
            System.out.println("Reporte generado exitosamente en: " + rutaArchivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}