package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Promoción tal como la devuelve la API (/api/promociones). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromocionDTO {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String id;
    private String tipo;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa;
    private boolean vigente;
    private List<PromocionReferenciaDTO> productos = new ArrayList<>();
    private List<PromocionReferenciaDTO> subCategorias = new ArrayList<>();
    private BigDecimal porcentaje;
    private List<PromocionTramoDTO> tramos = new ArrayList<>();
    private String codigo;
    private String tipoValor;
    private BigDecimal valor;
    private BigDecimal montoMinimo;
    private Integer usosMaximos;
    private int usosRealizados;
    private boolean acumulable;
    private String resumen;

    public String getTipoLegible() {
        return switch (tipo == null ? "" : tipo) {
            case "PORCENTAJE_PRODUCTO" -> "Porcentaje por producto";
            case "VOLUMEN" -> "Por volumen";
            case "CODIGO" -> "Código de descuento";
            default -> tipo;
        };
    }

    public String getVigenciaLegible() {
        return (fechaInicio == null ? "?" : fechaInicio.format(FECHA)) + " al " + (fechaFin == null ? "?" : fechaFin.format(FECHA));
    }

    /** Estado para la insignia: Vigente, Programada, Vencida o Desactivada. */
    public String getEstadoLegible() {
        if (!activa) {
            return "Desactivada";
        }
        if (vigente) {
            return "Vigente";
        }
        return fechaInicio != null && fechaInicio.isAfter(LocalDate.now()) ? "Programada" : "Vencida";
    }

    public String getEstadoTono() {
        return switch (getEstadoLegible()) {
            case "Vigente" -> "insignia-verde";
            case "Programada" -> "insignia-azul";
            case "Vencida" -> "insignia-gris";
            default -> "insignia-naranja";
        };
    }

    /** Productos y subcategorías alcanzados, o "Todo el pedido" para un código sin restricción. */
    public String getAlcanceLegible() {
        List<String> partes = new ArrayList<>();
        productos.forEach(p -> partes.add(p.getNombre()));
        subCategorias.forEach(s -> partes.add("Subcategoría " + s.getNombre()));
        return partes.isEmpty() ? "Todo el pedido" : String.join(", ", partes);
    }

    public String getUsosLegible() {
        return usosMaximos == null ? usosRealizados + " (sin límite)" : usosRealizados + " de " + usosMaximos;
    }
}
