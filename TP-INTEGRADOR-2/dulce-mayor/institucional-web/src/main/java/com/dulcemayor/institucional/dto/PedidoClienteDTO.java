package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** Pedido del cliente tal como lo devuelve el portal (/api/cliente/pedidos). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoClienteDTO {

    private String id;
    private String nombreComercial;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private String promocionCodigo;
    private BigDecimal total;
    private String fechaCreacion;
    private String fechaConfirmacion;
    private String fechaCompletado;
    private List<LineaPedidoClienteDTO> lineas;

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "BORRADOR" -> "En preparación";
            case "CONFIRMADO" -> "Reservado, esperando pago";
            case "COMPLETADO" -> "Pago acreditado";
            case "ENTREGADO" -> "Entregado";
            case "CANCELADO" -> "Cancelado";
            default -> estado;
        };
    }

    public String getFechaReferencia() {
        String fecha = fechaConfirmacion != null ? fechaConfirmacion : fechaCreacion;
        return fecha == null ? "" : fecha.replace("T", " ").substring(0, Math.min(16, fecha.length()));
    }

    public boolean isConComprobante() {
        return "COMPLETADO".equals(estado) || "ENTREGADO".equals(estado);
    }
}
