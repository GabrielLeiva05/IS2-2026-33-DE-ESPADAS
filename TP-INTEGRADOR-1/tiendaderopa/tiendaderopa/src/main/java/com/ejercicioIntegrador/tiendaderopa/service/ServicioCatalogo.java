package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioCatalogo {

    private final ServicioProducto productoServicio;
    private final ServicioVigenciaPrecio vigenciaPrecioServicio;
    private final ServicioStock stockServicio;

    public ServicioCatalogo(ServicioProducto productoServicio,
            ServicioVigenciaPrecio vigenciaPrecioServicio, ServicioStock stockServicio) {
        this.productoServicio = productoServicio;
        this.vigenciaPrecioServicio = vigenciaPrecioServicio;
        this.stockServicio = stockServicio;
    }

    @Transactional(readOnly = true)
    public List<ProductoCatalogoDTO> listarDisponibles() {
        return productoServicio.listarProductoActivo().stream()
                .filter(producto -> !producto.isEliminado())
                .map(this::aDTO)
                .toList();
    }

    private ProductoCatalogoDTO aDTO(Producto producto) {
        VigenciaPrecio vigente = vigenciaPrecioServicio.buscarVigenciaPrecioVigente(producto.getId());
        Stock stock = stockServicio.buscarStockActual(producto.getId());
        double precio = vigente == null || vigente.isEliminado() || vigente.getFechaDesde().isAfter(LocalDate.now())
                ? 0 : vigente.getPrecio();
        return new ProductoCatalogoDTO(producto, precio, stock == null ? 0 : stock.getCantActual());
    }
}