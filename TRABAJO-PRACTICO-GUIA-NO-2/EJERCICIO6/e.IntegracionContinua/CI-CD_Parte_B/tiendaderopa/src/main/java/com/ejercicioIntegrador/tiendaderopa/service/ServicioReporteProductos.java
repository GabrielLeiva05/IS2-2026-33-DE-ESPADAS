package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoStock;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ProductoStockDTO;
import com.ejercicioIntegrador.tiendaderopa.dto.reportes.ReporteProductosDTO;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
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
    private ServicioSucursal svcSucursal;

    @Autowired
    private ServicioObjetivoReposicion svcObjetivoReposicion;

    @Autowired
    private ServicioReporteProveedores svcReporteProveedores;

    @Autowired
    private ServicioWhatsapp svcWhatsapp;

    public ReporteProductosDTO generarReporte() {
        return generarReporte(svcSucursal.obtenerPrincipal());
    }

    public ReporteProductosDTO generarReporte(String sucursalId) {
        return generarReporte(svcSucursal.buscarActiva(sucursalId));
    }

    private ReporteProductosDTO generarReporte(Sucursal sucursal) {
        Collection<Producto> productos = svcProducto.listarProductoActivo();

        List<ProductoStockDTO> items = productos.stream()
            .map(producto -> aDTO(producto, sucursal))
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

    private ProductoStockDTO aDTO(Producto producto, Sucursal sucursal) {
        int cantidadActual = svcStock.cantidadActual(producto.getId(), sucursal.getId());
        int stockMaximo = svcObjetivoReposicion.obtenerCantidadObjetivo(
                sucursal.getId(), producto.getId(), producto.getStockMaximo());
        SubCategoria subCategoria = producto.getSubCategoria();

        ProductoStockDTO dto = new ProductoStockDTO();
        dto.setIdProducto(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setSubCategoria(subCategoria != null ? subCategoria.getNombre() : null);
        dto.setCategoria(subCategoria != null && subCategoria.getCategoria() != null
                ? subCategoria.getCategoria().getNombre()
                : null);
        dto.setSucursal(sucursal.getNombre());
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
            completarSugerenciaDeReposicion(dto, producto, sucursal.getId(), cantidadActual);
        }

        return dto;
    }

    private void completarSugerenciaDeReposicion(ProductoStockDTO dto, Producto producto, String sucursalId,
            int cantidadActual) {
        int cantidadSugerida = svcObjetivoReposicion.cantidadAReponer(
                sucursalId, producto.getId());
        dto.setCantidadSugeridaCompra(cantidadSugerida);

        svcReporteProveedores.buscarProveedorMasEconomico(producto.getId())
                .ifPresent(proveedor -> {
                    dto.setSugerenciaProveedorRazonSocial(proveedor.getRazonSocial());
                    String mensaje = armarMensajeReposicion(
                            proveedor.getRazonSocial(), producto.getNombre(), cantidadActual, cantidadSugerida);
                    dto.setSugerenciaProveedorWhatsappLink(
                            svcWhatsapp.armarLinkWhatsappWeb(proveedor.getTelefonoWhatsapp(), mensaje));
                });
    }

    private String armarMensajeReposicion(String razonSocial, String nombreProducto,
            int stockActual, int cantidadSugerida) {
        return "Hola " + razonSocial + ", te escribimos de Zero. Nos quedan " + stockActual
                + " unidades de \"" + nombreProducto + "\". Por favor, enviános " + cantidadSugerida
                + " unidades para reponer el stock. ¡Gracias!";
    }

    private EstadoStock clasificar(double proporcion) {
        if (proporcion > UMBRAL_BUENO)
            return EstadoStock.BUENO;
        if (proporcion >= UMBRAL_REGULAR)
            return EstadoStock.REGULAR;
        return EstadoStock.MALO;
    }

    private int contar(List<ProductoStockDTO> items, EstadoStock estado) {
        return (int) items.stream().filter(i -> i.getEstado() == estado).count();
    }
}