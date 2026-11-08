package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ApiException;
import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.CategoriaDTO;
import com.dulcemayor.admin.dto.FilaProductoDTO;
import com.dulcemayor.admin.dto.ImagenProductoDTO;
import com.dulcemayor.admin.dto.PrecioDTO;
import com.dulcemayor.admin.dto.ProductoDTO;
import com.dulcemayor.admin.dto.ProductoFormularioDTO;
import com.dulcemayor.admin.dto.ProductoSolicitudDTO;
import com.dulcemayor.admin.dto.StockDTO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ABM de productos (corrección Ola 2) para ADMINISTRATIVO y JEFE. Reutiliza la API del módulo de Catálogo y Stock:
 * /api/productos (alta, modificación, baja lógica), /api/vigencias-precio (precio), /api/stock (stock) e
 * /api/imagenes (fotos). No hay un CRUD paralelo: la API valida cada regla y los permisos.
 */
@Service
public class ServicioProductos {

    private final ClienteApi clienteApi;

    public ServicioProductos(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    /** Productos activos con su precio vigente y su stock, ordenados por nombre. */
    public List<FilaProductoDTO> listar(String token, String texto) {
        List<ProductoDTO> productos = clienteApi.listar(token, ProductoDTO[].class, "/api/productos");
        Map<String, StockDTO> stock = clienteApi.listar(token, StockDTO[].class, "/api/stock").stream()
                .collect(Collectors.toMap(StockDTO::getIdProducto, Function.identity(), (a, b) -> a));
        Map<String, Double> precios = clienteApi.listarConFiltros(token,
                        com.dulcemayor.admin.dto.ProductoCatalogoDTO[].class, "/api/catalogo", Map.of()).stream()
                .collect(Collectors.toMap(com.dulcemayor.admin.dto.ProductoCatalogoDTO::getId,
                        com.dulcemayor.admin.dto.ProductoCatalogoDTO::getPrecio, (a, b) -> a));
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        return productos.stream()
                .filter(p -> filtro.isEmpty() || p.getNombre().toLowerCase().contains(filtro)
                        || p.getCodigo().toLowerCase().contains(filtro))
                .sorted(Comparator.comparing(ProductoDTO::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(p -> new FilaProductoDTO(p, precios.containsKey(p.getId()) ? precios.get(p.getId())
                        : precioVigente(token, p.getId()).map(PrecioDTO::getPrecio).orElse(null), stock.get(p.getId())))
                .toList();
    }

    public ProductoDTO obtener(String token, String id) {
        return clienteApi.obtener(token, ProductoDTO.class, "/api/productos/{id}", id);
    }

    public Optional<StockDTO> stock(String token, String idProducto) {
        try {
            return Optional.ofNullable(clienteApi.obtener(token, StockDTO.class, "/api/stock/producto/{id}", idProducto));
        } catch (ApiException e) {
            if (e.getEstado() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    public List<PrecioDTO> precios(String token, String idProducto) {
        return clienteApi.listar(token, PrecioDTO[].class, "/api/vigencias-precio/producto/{id}", idProducto).stream()
                .sorted(Comparator.comparing(PrecioDTO::getFechaDesde).reversed())
                .toList();
    }

    public List<CategoriaDTO> categorias(String token) {
        return clienteApi.listar(token, CategoriaDTO[].class, "/api/categorias");
    }

    public List<CategoriaDTO> subcategorias(String token, String idCategoria) {
        if (idCategoria == null || idCategoria.isBlank()) {
            return List.of();
        }
        return clienteApi.listar(token, CategoriaDTO[].class, "/api/subcategorias/categoria/{id}", idCategoria);
    }

    /**
     * Alta: crea el producto y, si se informaron, su precio inicial (vigente desde hoy) y su stock.
     * Si falla un paso posterior al alta del producto, el producto queda creado y el error se informa para
     * completarlo desde la edición (cada paso es una operación de la API).
     */
    public ProductoDTO crear(String token, ProductoFormularioDTO f) {
        ProductoDTO creado = clienteApi.enviar(token, aSolicitud(f), ProductoDTO.class, "/api/productos");
        if (f.getPrecio() != null) {
            nuevoPrecio(token, creado.getId(), f.getPrecio(), null);
        }
        if (f.getStockInicial() != null || f.getStockMinimo() != null) {
            clienteApi.enviar(token, Map.of("idProducto", creado.getId(),
                    "cantidadInicial", f.getStockInicial() == null ? 0 : f.getStockInicial(),
                    "stockMinimo", f.getStockMinimo() == null ? 0 : f.getStockMinimo()), StockDTO.class, "/api/stock");
        }
        return creado;
    }

    public ProductoDTO modificar(String token, String id, ProductoFormularioDTO f) {
        return clienteApi.reemplazar(token, aSolicitud(f), ProductoDTO.class, "/api/productos/{id}", id);
    }

    /** Baja lógica: la API la rechaza si el producto está en pedidos sin terminar. */
    public void darDeBaja(String token, String id) {
        clienteApi.eliminar(token, "/api/productos/{id}", id);
    }

    /** Nuevo precio: la API cierra la vigencia anterior el día previo al inicio de la nueva. */
    public void nuevoPrecio(String token, String idProducto, Double precio, LocalDate desde) {
        clienteApi.enviar(token, Map.of("idProducto", idProducto, "precio", precio,
                "fechaDesde", (desde == null ? LocalDate.now() : desde).toString()), PrecioDTO.class, "/api/vigencias-precio");
    }

    /** Ingreso de mercadería: suma unidades al stock (si el producto no tenía stock, lo crea). */
    public void reponerStock(String token, String idProducto, int cantidad, Integer stockMinimo) {
        if (stock(token, idProducto).isPresent()) {
            clienteApi.parchear(token, Map.of("cantidad", cantidad), StockDTO.class,
                    "/api/stock/producto/{id}/reponer", idProducto);
        } else {
            clienteApi.enviar(token, Map.of("idProducto", idProducto, "cantidadInicial", cantidad,
                    "stockMinimo", stockMinimo == null ? 0 : stockMinimo), StockDTO.class, "/api/stock");
        }
    }

    /** Sube una foto (tipo PRODUCTO) y devuelve su id para asociarla al producto. */
    public String subirImagen(String token, MultipartFile archivo) {
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        try {
            byte[] contenido = archivo.getBytes();
            String nombre = archivo.getOriginalFilename() == null ? "imagen" : archivo.getOriginalFilename();
            partes.add("archivo", new ByteArrayResource(contenido) {
                @Override
                public String getFilename() {
                    return nombre;
                }
            });
        } catch (IOException e) {
            throw new ApiException(400, "No se pudo leer la imagen elegida", null);
        }
        partes.add("tipoImagen", "PRODUCTO");
        ImagenProductoDTO imagen = clienteApi.enviarArchivo(token, partes, ImagenProductoDTO.class, "/api/imagenes/subir");
        return imagen.getId();
    }

    private Optional<PrecioDTO> precioVigente(String token, String idProducto) {
        try {
            return Optional.ofNullable(clienteApi.obtener(token, PrecioDTO.class,
                    "/api/vigencias-precio/producto/{id}/vigente", idProducto));
        } catch (ApiException e) {
            if (e.getEstado() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private static ProductoSolicitudDTO aSolicitud(ProductoFormularioDTO f) {
        return new ProductoSolicitudDTO(texto(f.getCodigo()), texto(f.getNombre()), texto(f.getDescripcion()),
                texto(f.getPresentacion()), f.getUnidadesPorPresentacion(), f.isEnOferta(), texto(f.getIdSubCategoria()),
                f.getIdsImagen() == null ? List.of() : f.getIdsImagen().stream().filter(id -> id != null && !id.isBlank()).toList());
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
