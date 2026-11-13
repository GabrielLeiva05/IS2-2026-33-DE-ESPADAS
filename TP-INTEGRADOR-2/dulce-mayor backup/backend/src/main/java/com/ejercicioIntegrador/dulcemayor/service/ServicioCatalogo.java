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

    public ServicioCatalogo(ServicioProducto servicioProducto, ServicioVigenciaPrecio servicioVigenciaPrecio,
                            ServicioStock servicioStock) {
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioStock = servicioStock;
    }

    /** listarProductoConStock() del diagrama, con los filtros opcionales del catálogo. */
    @Transactional(readOnly = true)
    public List<ProductoCatalogoDTO> listarProductoConStock(FiltroCatalogoDTO filtro) {
        FiltroCatalogoDTO criterios = filtro == null ? new FiltroCatalogoDTO(null, null, null, false) : filtro;
        List<ProductoCatalogoDTO> disponibles = new ArrayList<>();
        for (Producto producto : servicioProducto.listarEntidadesActivas()) {
            if (clasificacionActiva(producto) && cumpleFiltro(producto, criterios)) {
                armarItem(producto).ifPresent(disponibles::add);
            }
        }
        disponibles.sort(Comparator.comparing(ProductoCatalogoDTO::nombre, String.CASE_INSENSITIVE_ORDER));
        return disponibles;
    }

    @Transactional(readOnly = true)
    public ProductoCatalogoDTO consultarProductoDisponible(String idProducto) throws MiException {
        Producto producto = servicioProducto.obtenerEntidad(idProducto);
        Optional<ProductoCatalogoDTO> item = clasificacionActiva(producto) ? armarItem(producto) : Optional.empty();
        return item.orElseThrow(() -> new RecursoNoEncontradoException("El producto no está disponible en el catálogo"));
    }

    private Optional<ProductoCatalogoDTO> armarItem(Producto producto) {
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
        List<String> idsImagen = producto.getImagenes().stream()
                .filter(imagen -> !imagen.isEliminado())
                .map(Imagen::getId)
                .toList();
        return Optional.of(new ProductoCatalogoDTO(producto.getId(), producto.getCodigo(), producto.getNombre(),
                producto.getDescripcion(), producto.getPresentacion(), producto.getUnidadesPorPresentacion(),
                categoria.getId(), categoria.getNombre(), subCategoria.getId(), subCategoria.getNombre(),
                producto.isEnOferta(), precio.get().getPrecio(), stockDisponible, idsImagen));
    }

    private static boolean clasificacionActiva(Producto producto) {
        SubCategoria subCategoria = producto.getSubCategoria();
        return subCategoria != null && !subCategoria.isEliminado()
                && subCategoria.getCategoria() != null && !subCategoria.getCategoria().isEliminado();
    }

    private static boolean cumpleFiltro(Producto producto, FiltroCatalogoDTO filtro) {
        SubCategoria subCategoria = producto.getSubCategoria();
        if (tieneValor(filtro.idCategoria()) && !filtro.idCategoria().equals(subCategoria.getCategoria().getId())) {
            return false;
        }
        if (tieneValor(filtro.idSubCategoria()) && !filtro.idSubCategoria().equals(subCategoria.getId())) {
            return false;
        }
        if (filtro.soloOfertas() && !producto.isEnOferta()) {
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
