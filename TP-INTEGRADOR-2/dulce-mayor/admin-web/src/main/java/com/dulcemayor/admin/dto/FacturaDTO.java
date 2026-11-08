package com.dulcemayor.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Factura de cliente o de proveedor (GET /api/facturas/clientes y /api/facturas/proveedores). */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaDTO {

    private String id;
    private String tipo;
    private long numeroFactura;
    private String fechaFactura;
    private String estado;
    private double subtotal;
    private double descuento;
    private double iva;
    private double alicuotaIva;
    private double total;
    private Long numeroPedido;
    private String nombreCliente;
    private String nombreFantasia;
    private String razonSocialProveedor;
    private String cuitProveedor;
    private List<DetalleFacturaDTO> detalles = new ArrayList<>();

    /** "2026-11-06T12:02:00" → "06/11/2026". */
    public String getFechaLegible() {
        return fechaFactura == null || fechaFactura.length() < 10 ? ""
                : fechaFactura.substring(8, 10) + "/" + fechaFactura.substring(5, 7) + "/" + fechaFactura.substring(0, 4);
    }

    public String getEstadoLegible() {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "PENDIENTE_PAGO" -> "Pendiente de pago";
            case "PAGADA" -> "Pagada";
            case "ANULADA" -> "Anulada";
            default -> estado;
        };
    }

    public String getEstadoTono() {
        return "PAGADA".equals(estado) ? "insignia-verde" : "ANULADA".equals(estado) ? "insignia-gris" : "insignia-naranja";
    }
}
