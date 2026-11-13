package com.dulcemayor.institucional.service;

import com.dulcemayor.institucional.cliente.ClienteApi;
import com.dulcemayor.institucional.cliente.ApiException;
import com.dulcemayor.institucional.dto.CategoriaDTO;
import com.dulcemayor.institucional.dto.CotizacionDTO;
import com.dulcemayor.institucional.dto.ProductoCatalogoDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/** Catálogo público: productos con precio vigente (módulo Catálogo y Stock), sin necesidad de sesión. */
@Service
public class ServicioCatalogo {

    private static final int DESTACADOS = 8;

    private final ClienteApi clienteApi;

    public ServicioCatalogo(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<ProductoCatalogoDTO> buscar(String idCategoria, String texto, boolean soloOfertas) {
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("idCategoria", idCategoria);
        filtros.put("texto", texto);
        filtros.put("soloOfertas", soloOfertas ? "true" : null);
        return clienteApi.listarConFiltros(null, ProductoCatalogoDTO[].class, "/api/catalogo", filtros);
    }

    public List<ProductoCatalogoDTO> ofertasDestacadas() {
        return buscar(null, null, true).stream().limit(DESTACADOS).toList();
    }

    /** Productos con stock primero, para la portada. */
    public List<ProductoCatalogoDTO> destacados() {
        return buscar(null, null, false).stream()
                .sorted((a, b) -> Boolean.compare(b.getStockDisponible() > 0, a.getStockDisponible() > 0))
                .limit(DESTACADOS)
                .toList();
    }

    /**
     * Cotización del dólar que usa la API para los precios en USD. Si no está disponible (503) o la API no
     * responde, devuelve vacío: el sitio se muestra igual, solo en pesos.
     */
    public Optional<CotizacionDTO> cotizacion() {
        try {
            return Optional.ofNullable(clienteApi.obtener(null, CotizacionDTO.class, "/api/cotizacion"));
        } catch (ApiException e) {
            return Optional.empty();
        }
    }

    public List<CategoriaDTO> categorias() {
        return clienteApi.listar(null, CategoriaDTO[].class, "/api/categorias");
    }
}
