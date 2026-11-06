package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Pedido del cliente tal como lo devuelve la API. Las acciones posibles (puedeConfirmarse, puedeAnularse)
 * las decide el backend: la vista solo muestra u oculta los botones.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoDTO {

    private String id;
    private long numeroPedido;
    private String fechaHoraCarga;
    private String fechaConfirmacion;
    private String fechaEntrega;
    private double subtotal;
    private double descuento;
    private double total;
    private String estado;
    private boolean confirmado;
    private boolean puedeConfirmarse;
    private boolean puedeAnularse;
    private String nombreFantasia;
    private String direccion;
    private String nombrePreventista;
    private List<DetallePedidoDTO> detalles = List.of();

    public String getCargaLegible() {
        return legible(fechaHoraCarga);
    }

    public String getConfirmacionLegible() {
        return legible(fechaConfirmacion);
    }

    public String getEntregaLegible() {
        return legible(fechaEntrega);
    }

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "CARGADO" -> "Pendiente de tu confirmación";
            case "PENDIENTE_DE_ENTREGA" -> "Confirmado, pendiente de entrega";
            case "ENTREGADO" -> "Entregado";
            case "ANULADO" -> "Anulado";
            default -> estado;
        };
    }

    /** Clase de la insignia según el estado. */
    public String getEstadoTono() {
        if (estado == null) {
            return "insignia-gris";
        }
        return switch (estado) {
            case "CARGADO" -> "insignia-naranja";
            case "PENDIENTE_DE_ENTREGA" -> "insignia-azul";
            case "ENTREGADO" -> "insignia-verde";
            default -> "insignia-gris";
        };
    }

    public int getCantidadBultos() {
        return detalles == null ? 0 : detalles.stream().mapToInt(DetallePedidoDTO::getCantidad).sum();
    }

    /** "2026-11-03T10:15:30" -> "03/11/2026 10:15". */
    private static String legible(String fecha) {
        if (fecha == null || fecha.length() < 16) {
            return null;
        }
        return fecha.substring(8, 10) + "/" + fecha.substring(5, 7) + "/" + fecha.substring(0, 4) + " "
                + fecha.substring(11, 16);
    }
}
