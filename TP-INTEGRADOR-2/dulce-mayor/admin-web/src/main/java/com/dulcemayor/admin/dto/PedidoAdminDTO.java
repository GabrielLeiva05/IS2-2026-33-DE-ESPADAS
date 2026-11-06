package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Pedido tal como lo devuelve la API (PedidoResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoAdminDTO {

    private String id;
    private String idPuntoVenta;
    private String nombreComercial;
    private String idPreventista;
    private String estado;
    private BigDecimal total;
    private String fechaCreacion;
    private String fechaConfirmacion;
    private String fechaCancelacion;
    private String fechaCompletado;
    private List<LineaPedidoAdminDTO> lineas;

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "BORRADOR" -> "Borrador";
            case "CONFIRMADO" -> "Confirmado (reservado)";
            case "CANCELADO" -> "Cancelado";
            case "COMPLETADO" -> "Completado";
            default -> estado;
        };
    }

    public String getClaseEstado() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "CONFIRMADO" -> "insignia-azul";
            case "COMPLETADO" -> "insignia-verde";
            case "CANCELADO" -> "insignia-roja";
            default -> "insignia-gris";
        };
    }

    public String getFechaReferencia() {
        String fecha = fechaConfirmacion != null ? fechaConfirmacion : fechaCreacion;
        return fecha == null ? "" : fecha.replace("T", " ").substring(0, Math.min(16, fecha.length()));
    }

    public boolean isPendienteDeCobro() {
        return "CONFIRMADO".equals(estado);
    }
}
