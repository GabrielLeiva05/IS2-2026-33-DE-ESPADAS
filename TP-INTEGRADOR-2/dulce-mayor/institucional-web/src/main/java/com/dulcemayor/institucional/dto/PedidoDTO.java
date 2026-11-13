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
    /** Factura emitida al confirmar (Ola 3): id, número y total con IVA. */
    private String idFactura;
    private Long numeroFactura;
    private Double totalFacturado;
    /** Pagos (Checkout API): medio elegido por el preventista y estado del pago. */
    private String formaDePago;
    private String estadoPago;
    private boolean confirmado;
    private boolean puedeConfirmarse;
    private boolean puedeAnularse;
    private String nombreFantasia;
    private String direccion;
    private String nombrePreventista;
    private List<DetallePedidoDTO> detalles = List.of();
    private List<DescuentoPedidoDTO> descuentos = List.of();
    private String codigoDescuento;

    public String getCargaLegible() {
        return legible(fechaHoraCarga);
    }

    public String getConfirmacionLegible() {
        return legible(fechaConfirmacion);
    }

    public String getEntregaLegible() {
        return legible(fechaEntrega);
    }

    public String getEstadoLegible() {        if (estado == null) {
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

    public String getFormaDePagoLegible() {
        if (formaDePago == null) {
            return null;
        }
        return switch (formaDePago) {
            case "EFECTIVO" -> "Efectivo (al entregar)";
            case "TRANSFERENCIA" -> "Transferencia";
            case "QR" -> "QR de Mercado Pago";
            default -> formaDePago;
        };
    }

    public String getEstadoPagoLegible() {
        if (estadoPago == null) {
            return "Pendiente";
        }
        return switch (estadoPago) {
            case "PENDIENTE" -> "Pendiente";
            case "APROBADO" -> "Pagado";
            case "RECHAZADO" -> "Rechazado";
            default -> estadoPago;
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
