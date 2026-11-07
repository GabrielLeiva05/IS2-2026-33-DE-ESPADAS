package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Promoción tal como la devuelve la API (PromocionResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromocionAdminDTO {

    private String id;
    private String codigo;
    private String descripcion;
    private String tipo;
    private BigDecimal valor;
    private String fechaDesde;
    private String fechaHasta;
    private boolean activa;
    private boolean usoUnico;
    private BigDecimal minimoCompra;
    private boolean vigente;

    public String getTipoLegible() {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "PORCENTAJE" -> "Porcentaje";
            case "MONTO_FIJO" -> "Monto fijo";
            default -> tipo;
        };
    }

    public String getValorLegible() {
        if (valor == null) {
            return "";
        }
        return "PORCENTAJE".equals(tipo)
                ? valor.stripTrailingZeros().toPlainString() + " %"
                : "$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
