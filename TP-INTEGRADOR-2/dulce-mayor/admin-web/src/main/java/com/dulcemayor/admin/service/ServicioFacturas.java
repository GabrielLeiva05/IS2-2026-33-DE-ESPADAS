package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.DireccionSolicitudDTO;
import com.dulcemayor.admin.dto.FacturaDTO;
import com.dulcemayor.admin.dto.FacturaProveedorFormularioDTO;
import com.dulcemayor.admin.dto.ProductoDTO;
import com.dulcemayor.admin.dto.ProveedorDTO;
import com.dulcemayor.admin.dto.ProveedorFormularioDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facturación en admin-web (Ola 3): facturas de clientes (solo consulta y PDF: se emiten al confirmar el pedido),
 * facturas de proveedores (carga y PDF) y ABM de proveedores. Todo por la API; no hay datos ni cálculos locales.
 */
@Service
public class ServicioFacturas {

    private final ClienteApi clienteApi;

    public ServicioFacturas(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<FacturaDTO> facturasDeClientes(String token) {
        return clienteApi.listar(token, FacturaDTO[].class, "/api/facturas/clientes");
    }

    public List<FacturaDTO> facturasDeProveedores(String token) {
        return clienteApi.listar(token, FacturaDTO[].class, "/api/facturas/proveedores");
    }

    public byte[] pdfCliente(String token, String id) {
        return clienteApi.obtenerArchivo(token, "/api/facturas/clientes/{id}/pdf", id);
    }

    public byte[] pdfProveedor(String token, String id) {
        return clienteApi.obtenerArchivo(token, "/api/facturas/proveedores/{id}/pdf", id);
    }

    public FacturaDTO cargarFacturaProveedor(String token, FacturaProveedorFormularioDTO f) {
        List<Map<String, Object>> renglones = new ArrayList<>();
        for (int i = 0; i < f.getIdProducto().size(); i++) {
            String idProducto = f.getIdProducto().get(i);
            if (idProducto == null || idProducto.isBlank()) {
                continue; // renglón vacío del formulario
            }
            Map<String, Object> renglon = new LinkedHashMap<>();
            renglon.put("idProducto", idProducto);
            renglon.put("cantidad", i < f.getCantidad().size() ? f.getCantidad().get(i) : null);
            renglon.put("precioUnitario", i < f.getPrecioUnitario().size() ? f.getPrecioUnitario().get(i) : null);
            renglones.add(renglon);
        }
        if (renglones.isEmpty()) {
            throw new ApiException(400, "Agregá al menos un producto a la factura", null);
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idProveedor", f.getIdProveedor());
        cuerpo.put("numeroFactura", f.getNumeroFactura());
        cuerpo.put("fechaFactura", f.getFechaFactura() == null || f.getFechaFactura().isBlank() ? null : f.getFechaFactura());
        cuerpo.put("detalles", renglones);
        return clienteApi.enviar(token, cuerpo, FacturaDTO.class, "/api/facturas/proveedores");
    }

    public List<ProductoDTO> productos(String token) {
        return clienteApi.listar(token, ProductoDTO[].class, "/api/productos").stream()
                .sorted(Comparator.comparing(ProductoDTO::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    // ---------------------------------------------------------------- proveedores

    public List<ProveedorDTO> proveedores(String token) {
        return clienteApi.listar(token, ProveedorDTO[].class, "/api/proveedores").stream()
                .sorted(Comparator.comparing(ProveedorDTO::getRazonSocial, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public ProveedorDTO proveedor(String token, String id) {
        return clienteApi.obtener(token, ProveedorDTO.class, "/api/proveedores/{id}", id);
    }

    public ProveedorDTO crearProveedor(String token, ProveedorFormularioDTO f) {
        return clienteApi.enviar(token, solicitud(f), ProveedorDTO.class, "/api/proveedores");
    }

    public ProveedorDTO modificarProveedor(String token, String id, ProveedorFormularioDTO f) {
        return clienteApi.reemplazar(token, solicitud(f), ProveedorDTO.class, "/api/proveedores/{id}", id);
    }

    public void darDeBajaProveedor(String token, String id) {
        clienteApi.eliminar(token, "/api/proveedores/{id}", id);
    }

    private static Map<String, Object> solicitud(ProveedorFormularioDTO f) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("razonSocial", texto(f.getRazonSocial()));
        cuerpo.put("cuit", texto(f.getCuit()));
        cuerpo.put("direccion", new DireccionSolicitudDTO(texto(f.getCalle()), texto(f.getNumeracion()), texto(f.getBarrio()),
                null, null, null, null, null, texto(f.getIdLocalidad())));
        return cuerpo;
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
