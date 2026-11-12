package com.ejercicioIntegrador.dulcemayor.exportacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Textos de encabezado compartidos por las estrategias (período, fecha de generación, filtros), para que el
 * PDF y los Excel muestren lo mismo sin repetir código. Solo formatea; no decide nada del negocio.
 */
final class TextoReporte {

    static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TextoReporte() {
    }

    static String fecha(LocalDate fecha, String vacio) {
        return fecha == null ? vacio : FECHA.format(fecha);
    }

    static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA_HORA.format(fecha);
    }

    /** Texto del período; un extremo sin informar se muestra como "inicio" o "hoy". */
    static String periodo(LocalDate desde, LocalDate hasta) {
        return "Del " + fecha(desde, "inicio") + " al " + fecha(hasta, "hoy");
    }

    /** Filtros opcionales aplicados, en una línea. Si no hay ninguno, indica que no se aplicaron. */
    static String filtros(String... etiquetasYValores) {
        List<String> aplicados = new ArrayList<>();
        for (int i = 0; i + 1 < etiquetasYValores.length; i += 2) {
            if (etiquetasYValores[i + 1] != null && !etiquetasYValores[i + 1].isBlank()) {
                aplicados.add(etiquetasYValores[i] + ": " + etiquetasYValores[i + 1]);
            }
        }
        return aplicados.isEmpty() ? "Sin filtros adicionales" : String.join(" | ", aplicados);
    }
}
