package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProductoProveedoresDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProveedorPrecioDTO;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioReporteProveedores {

    @Autowired
    private ServicioOrdenCompraProveedor svcOrdenCompraProveedor;
    @Autowired
    private ServicioContactoTelefonico svcContactoTelefonico;
    @Autowired
    private ServicioWhatsapp svcWhatsapp;

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

    public Optional<ProveedorPrecioDTO> buscarProveedorMasEconomico(String idProducto) {
        return svcOrdenCompraProveedor.buscarDetallePorProductoOrdenadoPorPrecio(idProducto)
                .stream()
                .findFirst()
                .map(this::aDTO);
    }

    private ProveedorPrecioDTO aDTO(DetalleOrdenCompraProveedor detalle) {
        Proveedor proveedor = detalle.getOrdenCompraProveedor().getProveedor();
        String telefono = svcContactoTelefonico.buscarCelularDeProveedor(proveedor.getId())
                .map(svcWhatsapp::normalizarTelefono)
                .orElse(null);

        return new ProveedorPrecioDTO(
                proveedor.getId(),
                proveedor.getRazonSocial(),
                detalle.getPrecioCompra(),
                detalle.getOrdenCompraProveedor().getFecha(),
                telefono
        );
    }
}