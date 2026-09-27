package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoStock;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProductoStockDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ReporteProductosDTO;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioReporteProductos {

    private static final double UMBRAL_BUENO = 0.5;
    private static final double UMBRAL_REGULAR = 0.2;

    @Autowired
    private ServicioProducto svcProducto;
    @Autowired
    private ServicioStock svcStock;
    @Autowired
    private ServicioReporteProveedores svcReporteProveedores;

    public ReporteProductosDTO generarReporte() {
        Collection<Producto> productos = svcProducto.listarProductoActivo();

        List<ProductoStockDTO> items = productos.stream()
                .map(this::aDTO)
                .collect(Collectors.toList());

        ReporteProductosDTO reporte = new ReporteProductosDTO();
        reporte.setProductos(items);
        reporte.setCantidadProductos(items.size());
        reporte.setStockTotal(items.stream().mapToInt(ProductoStockDTO::getStockActual).sum());
        reporte.setCantidadBueno(contar(items, EstadoStock.BUENO));
        reporte.setCantidadRegular(contar(items, EstadoStock.REGULAR));
        reporte.setCantidadMalo(contar(items, EstadoStock.MALO));
        reporte.setCantidadSinDefinir(contar(items, EstadoStock.SIN_DEFINIR));
        return reporte;
    }

    private ProductoStockDTO aDTO(Producto producto) {
        Stock stockActual = svcStock.buscarStockActual(producto.getId());
        int cantidadActual = stockActual != null ? stockActual.getCantActual() : 0;
        int stockMaximo = producto.getStockMaximo();
        SubCategoria subCategoria = producto.getSubCategoria();

        ProductoStockDTO dto = new ProductoStockDTO();
        dto.setIdProducto(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setSubCategoria(subCategoria != null ? subCategoria.getNombre() : null);
        dto.setCategoria(subCategoria != null && subCategoria.getCategoria() != null
                ? subCategoria.getCategoria().getNombre() : null);
        dto.setStockActual(cantidadActual);
        dto.setStockMaximo(stockMaximo);

        if (stockMaximo <= 0) {
            // Sin objetivo de stock configurado todavía: no hay contra qué calcular el %.
            dto.setPorcentaje(null);
            dto.setEstado(EstadoStock.SIN_DEFINIR);
            return dto;
        }

        double proporcion = (double) cantidadActual / stockMaximo;
        dto.setPorcentaje(proporcion * 100);
        dto.setEstado(clasificar(proporcion));

        if (dto.getEstado() == EstadoStock.MALO) {
            completarSugerenciaDeReposicion(dto, producto, stockMaximo, cantidadActual);
        }

        return dto;
    }

    private void completarSugerenciaDeReposicion(ProductoStockDTO dto, Producto producto,
                                                  int stockMaximo, int cantidadActual) {
        int cantidadParaLlegarAlCincuenta = (int) Math.ceil(stockMaximo * UMBRAL_BUENO) - cantidadActual;
        dto.setCantidadSugeridaCompra(Math.max(cantidadParaLlegarAlCincuenta, 0));

        svcReporteProveedores.buscarProveedorMasEconomico(producto.getId())
                .ifPresent(proveedor -> {
                    dto.setSugerenciaProveedorRazonSocial(proveedor.getRazonSocial());
                    dto.setSugerenciaProveedorWhatsappLink(proveedor.getWhatsappLink());
                });
    }

    private EstadoStock clasificar(double proporcion) {
        if (proporcion > UMBRAL_BUENO) return EstadoStock.BUENO;
        if (proporcion >= UMBRAL_REGULAR) return EstadoStock.REGULAR;
        return EstadoStock.MALO;
    }

    private int contar(List<ProductoStockDTO> items, EstadoStock estado) {
        return (int) items.stream().filter(i -> i.getEstado() == estado).count();
    }
}