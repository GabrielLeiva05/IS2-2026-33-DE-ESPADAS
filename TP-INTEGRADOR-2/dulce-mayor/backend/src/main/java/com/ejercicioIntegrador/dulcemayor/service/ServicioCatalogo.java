package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FiltroCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.Imagen;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejercicioIntegrador.dulcemayor.model.Promocion;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ConversorMoneda;
import com.ejercicioIntegrador.dulcemayor.service.promociones.CalculadorDescuentos;
import com.ejercicioIntegrador.dulcemayor.service.promociones.OfertaCatalogo;
import com.ejercicioIntegrador.dulcemayor.service.promociones.RenglonDescuento;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ServicioCotizacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Catálogo público y del preventista (servicio reutilizado del Integrador 1, ServicioCatalogo).
 *
 * Un producto se muestra solo si:
 *  - no está dado de baja y su subcategoría y categoría están activas,
 *  - tiene stock disponible (mayor a cero): así deja de mostrarse automáticamente al quedarse sin stock,
 *  - tiene un precio mayorista vigente para la fecha actual.
 *
 * No hereda de BaseService porque no administra una entidad propia: solo consulta, y se comunica con
 * ServicioProducto, ServicioVigenciaPrecio y ServicioStock sin acceder a sus repositorios.
 */
@Service
public class ServicioCatalogo {

    private final ServicioProducto servicioProducto;
    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioStock servicioStock;
    private final ServicioCotizacion servicioCotizacion;
    private final ServicioPromocion servicioPromocion; // <-- Sin @Mock aquí

    public ServicioCatalogo(ServicioProducto servicioProducto, ServicioVigenciaPrecio servicioVigenciaPrecio,
                            ServicioStock servicioStock, ServicioCotizacion servicioCotizacion,
                            ServicioPromocion servicioPromocion) {
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioStock = servicioStock;
        this.servicioCotizacion = servicioCotizacion;
        this.servicioPromocion = servicioPromocion;
    }

    /** listarProductoConStock() del diagrama, con los filtros opcionales del catálogo. */
    @Transactional(readOnly = true)
    public List<ProductoCatalogoDTO> listarProductoConStock(FiltroCatalogoDTO filtro) {
        FiltroCatalogoDTO criterios = filtro == null ? new FiltroCatalogoDTO(null, null, null, false) : filtro;
        List<ProductoCatalogoDTO> disponibles = new ArrayList<>();
        // Una sola consulta de cotización por listado (en caché), nunca una por producto.
        BigDecimal cotizacion = servicioCotizacion.ventaVigente().orElse(null);
        List<Promocion> promociones = servicioPromocion.automaticasVigentes();
        for (Producto producto : servicioProducto.listarEntidadesActivas()) {
            if (clasificacionActiva(producto) && cumpleFiltro(producto, criterios, promociones)) {
                armarItem(producto, cotizacion, promociones).ifPresent(disponibles::add);
            }
        }
        disponibles.sort(Comparator.comparing(ProductoCatalogoDTO::nombre, String.CASE_INSENSITIVE_ORDER));
        return disponibles;
    }

    /**
     * Producto disponible con su precio vigente, sin consultar la cotización (lo usan los pedidos y el presupuesto,
     * que trabajan en pesos).
     */
    @Transactional(readOnly = true)
    public ProductoCatalogoDTO consultarProductoDisponible(String idProducto) throws MiException {
        return consultar(idProducto, null, List.of());
    }

    /** Producto disponible con su precio también en dólares (detalle del catálogo público). */
    @Transactional(readOnly = true)
    public ProductoCatalogoDTO consultarProductoConPrecios(String idProducto) throws MiException {
        return consultar(idProducto, servicioCotizacion.ventaVigente().orElse(null),
                servicioPromocion.automaticasVigentes());
    }

    private ProductoCatalogoDTO consultar(String idProducto, BigDecimal cotizacion, List<Promocion> promociones)
            throws MiException {
        Producto producto = servicioProducto.obtenerEntidad(idProducto);
        Optional<ProductoCatalogoDTO> item = clasificacionActiva(producto)
                ? armarItem(producto, cotizacion, promociones) : Optional.empty();
        return item.orElseThrow(() -> new RecursoNoEncontradoException("El producto no está disponible en el catálogo"));
    }

    private Optional<ProductoCatalogoDTO> armarItem(Producto producto, BigDecimal cotizacion,
                                                    List<Promocion> promociones) {
        int stockDisponible = servicioStock.cantidadDisponible(producto.getId());
        if (stockDisponible <= 0) {
            return Optional.empty();
        }
        Optional<VigenciaPrecio> precio = servicioVigenciaPrecio.obtenerVigenciaVigente(producto.getId());
        if (precio.isEmpty()) {
            return Optional.empty();
        }
        SubCategoria subCategoria = producto.getSubCategoria();
        Categoria categoria = subCategoria.getCategoria();
        OfertaCatalogo oferta = ServicioPromocion.ofertaPara(producto, promociones);
        BigDecimal precioLista = new RenglonDescuento(producto, 1, BigDecimal.valueOf(precio.get().getPrecio())).subtotal();
        BigDecimal precioPromocional = oferta.porcentajeUnitario().signum() <= 0 ? null
                : precioLista.subtract(CalculadorDescuentos.porcentajeDe(precioLista, oferta.porcentajeUnitario()));
        List<String> idsImagen = producto.getImagenes().stream()
                .filter(imagen -> !imagen.isEliminado())
                .map(Imagen::getId)
                .toList();
        return Optional.of(new ProductoCatalogoDTO(producto.getId(), producto.getCodigo(), producto.getNombre(),
                producto.getDescripcion(), producto.getPresentacion(), producto.getUnidadesPorPresentacion(),
                categoria.getId(), categoria.getNombre(), subCategoria.getId(), subCategoria.getNombre(),
                producto.isEnOferta(), precio.get().getPrecio(), stockDisponible, idsImagen,
                cotizacion == null ? null : ConversorMoneda.aDolares(precio.get().getPrecio(), cotizacion),
                precioPromocional, precioPromocional == null || cotizacion == null ? null
                : ConversorMoneda.aDolares(precioPromocional, cotizacion),
                oferta.textos()));
    }

    private static boolean clasificacionActiva(Producto producto) {
        SubCategoria subCategoria = producto.getSubCategoria();
        return subCategoria != null && !subCategoria.isEliminado()
                && subCategoria.getCategoria() != null && !subCategoria.getCategoria().isEliminado();
    }

    /** "Solo ofertas": marcados en oferta o con alguna promoción automática vigente. */
    private static boolean cumpleFiltro(Producto producto, FiltroCatalogoDTO filtro, List<Promocion> promociones) {
        SubCategoria subCategoria = producto.getSubCategoria();
        if (tieneValor(filtro.idCategoria()) && !filtro.idCategoria().equals(subCategoria.getCategoria().getId())) {
            return false;
        }
        if (tieneValor(filtro.idSubCategoria()) && !filtro.idSubCategoria().equals(subCategoria.getId())) {
            return false;
        }
        if (filtro.soloOfertas() && !producto.isEnOferta()
                && promociones.stream().noneMatch(p -> p.alcanza(producto))) {
            return false;
        }
        if (tieneValor(filtro.texto())) {
            String texto = filtro.texto().trim().toLowerCase();
            return contiene(producto.getNombre(), texto)
                    || contiene(producto.getDescripcion(), texto)
                    || contiene(producto.getCodigo(), texto);
        }
        return true;
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static boolean contiene(String campo, String textoEnMinusculas) {
        return campo != null && campo.toLowerCase().contains(textoEnMinusculas);
    }
}