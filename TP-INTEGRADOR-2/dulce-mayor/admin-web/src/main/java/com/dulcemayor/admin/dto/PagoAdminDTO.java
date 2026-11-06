package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Pago tal como lo devuelve la API (PagoResponseDTO). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoAdminDTO {

    private String id;
    private String idPedido;
    private String medio;
    private String estado;
    private BigDecimal importe;
    private String referenciaOperacion;
    private String referenciaExterna;
    private String instrucciones;
    private String enlacePago;
    private String fechaCreacion;
    private String fechaVencimiento;
    private String fechaAcreditacion;
    private String observacion;

    public String getMedioLegible() {
        if (medio == null) {
            return "";
        }
        return switch (medio) {
            case "TRANSFERENCIA" -> "Transferencia";
            case "MERCADO_PAGO" -> "Mercado Pago";
            case "EFECTIVO" -> "Efectivo";
            default -> medio;
        };
    }

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "ACREDITADO" -> "Acreditado";
            case "RECHAZADO" -> "Rechazado";
            case "EXPIRADO" -> "Vencido";
            case "CANCELADO" -> "Cancelado";
            default -> estado;
        };
    }

    public String getClaseEstado() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "ACREDITADO" -> "insignia-verde";
            case "PENDIENTE" -> "insignia-ambar";
            case "RECHAZADO", "EXPIRADO" -> "insignia-roja";
            default -> "insignia-gris";
        };
    }

    public String getFechaReferencia() {
        String fecha = fechaAcreditacion != null ? fechaAcreditacion : fechaCreacion;
        return fecha == null ? "" : fecha.replace("T", " ").substring(0, Math.min(16, fecha.length()));
    }

    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }
}
