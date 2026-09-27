package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProductoProveedoresDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProveedorPrecioDTO;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioReporteProveedores {

    @Autowired
    private ServicioOrdenCompraProveedor svcOrdenCompraProveedor;
    @Autowired
    private ServicioContactoTelefonico svcContactoTelefonico;

    public ProductoProveedoresDTO generarReportePorProducto(Producto producto) {
        List<ProveedorPrecioDTO> proveedores = svcOrdenCompraProveedor
                .buscarDetallePorProductoOrdenadoPorPrecio(producto.getId())
                .stream()
                .map(this::aDTO)
                .collect(Collectors.toList());

        ProductoProveedoresDTO dto = new ProductoProveedoresDTO();
        dto.setIdProducto(producto.getId());
        dto.setCodigoProducto(producto.getCodigo());
        dto.setNombreProducto(producto.getNombre());
        dto.setProveedores(proveedores);
        dto.setProveedorMasEconomico(proveedores.isEmpty() ? null : proveedores.get(0));
        return dto;
    }

    /**
     * Usado desde ServicioReporteProductos para sugerir a quien comprarle
     * cuando el stock de un producto esta en estado MALO.
     */
    public Optional<ProveedorPrecioDTO> buscarProveedorMasEconomico(String idProducto) {
        return svcOrdenCompraProveedor.buscarDetallePorProductoOrdenadoPorPrecio(idProducto)
                .stream()
                .findFirst()
                .map(this::aDTO);
    }

    private ProveedorPrecioDTO aDTO(DetalleOrdenCompraProveedor detalle) {
        Proveedor proveedor = detalle.getOrdenCompraProveedor().getProveedor();
        String whatsapp = svcContactoTelefonico.buscarCelularDeProveedor(proveedor.getId())
                .map(telefono -> armarLinkWhatsapp(telefono, detalle.getProducto().getNombre()))
                .orElse(null);

        return new ProveedorPrecioDTO(
                proveedor.getId(),
                proveedor.getRazonSocial(),
                detalle.getPrecioCompra(),
                detalle.getOrdenCompraProveedor().getFecha(),
                whatsapp
        );
    }

    /**
     * Arma un link https://wa.me/... con un mensaje precargado pidiendo
     * la reposicion del producto.
     *
     * Normalizacion de numero simplificada para Argentina: si el
     * telefono cargado no viene ya con codigo de pais, se le antepone
     * "549" (prefijo de celular argentino para WhatsApp). En un caso
     * real conviene normalizar y validar el telefono al cargar el
     * proveedor, no aca.
     */
    private String armarLinkWhatsapp(String telefono, String nombreProducto) {
        String soloDigitos = telefono.replaceAll("\\D", "");
        String numero = soloDigitos.startsWith("54") ? soloDigitos : "549" + soloDigitos;
        String mensaje = "Hola, necesitamos reponer stock de \"" + nombreProducto + "\". ¿Nos podés cotizar?";
        return "https://wa.me/" + numero + "?text=" + UriUtils.encode(mensaje, StandardCharsets.UTF_8);
    }
}
