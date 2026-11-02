package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.CategoriaDTO;
import com.dulcemayor.admin.dto.ProductoCatalogoDTO;
import com.dulcemayor.admin.dto.ProductoDTO;
import com.dulcemayor.admin.dto.StockDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consulta de solo lectura del catálogo con precio vigente y stock (módulo Catálogo y Stock). */
@Service
public class ServicioCatalogo {

    private final ClienteApi clienteApi;

    public ServicioCatalogo(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<ProductoCatalogoDTO> listarProductos(String token, String idCategoria, String texto, boolean soloOfertas) {
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("idCategoria", idCategoria);
        filtros.put("texto", texto);
        filtros.put("soloOfertas", soloOfertas ? "true" : null);
        return clienteApi.listarConFiltros(token, ProductoCatalogoDTO[].class, "/api/catalogo", filtros);
    }

    /** Productos dados de alta (con o sin precio vigente). Solo administración. */
    public List<ProductoDTO> listarProductosActivos(String token) {
        return clienteApi.listar(token, ProductoDTO[].class, "/api/productos");
    }

    /** Stock de todos los productos activos. Solo administración. */
    public List<StockDTO> listarStock(String token) {
        return clienteApi.listar(token, StockDTO[].class, "/api/stock");
    }

    public List<CategoriaDTO> listarCategorias(String token) {
        return clienteApi.listar(token, CategoriaDTO[].class, "/api/categorias");
    }
}
