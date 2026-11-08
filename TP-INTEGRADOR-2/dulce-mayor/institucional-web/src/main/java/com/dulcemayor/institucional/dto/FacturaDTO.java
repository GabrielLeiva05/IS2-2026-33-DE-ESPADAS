package com.dulcemayor.institucional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Factura del cliente (GET /api/facturas/mis-facturas). Importes: subtotal sin IVA, IVA y total con IVA. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaDTO {

    private String id;
    private long numeroFactura;
    private String fechaFactura;
    private String estado;
    private double subtotal;
    private double descuento;
    private double iva;
    private double total;
    private String idPedido;
    private Long numeroPedido;
    private String nombreFantasia;

    public String getFechaLegible() {
        return fechaFactura == null || fechaFactura.length() < 10 ? ""
                : fechaFactura.substring(8, 10) + "/" + fechaFactura.substring(5, 7) + "/" + fechaFactura.substring(0, 4);
    }

    public String getEstadoLegible() {
        return "PAGADA".equals(estado) ? "Pagada" : "ANULADA".equals(estado) ? "Anulada" : "Pendiente de pago";
    }

    public String getEstadoTono() {
        return "PAGADA".equals(estado) ? "insignia-verde" : "ANULADA".equals(estado) ? "insignia-gris" : "insignia-naranja";
    }
}
