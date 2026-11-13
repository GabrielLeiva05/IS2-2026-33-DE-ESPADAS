package com.dulcemayor.admin.service;

import com.dulcemayor.admin.cliente.ClienteApi;
import com.dulcemayor.admin.dto.CategoriaDTO;
import com.dulcemayor.admin.dto.ProductoDTO;
import com.dulcemayor.admin.dto.PromocionDTO;
import com.dulcemayor.admin.dto.PromocionFormularioDTO;
import com.dulcemayor.admin.dto.PromocionSolicitudDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Promociones y códigos de descuento (módulo de promociones de la API). Las reglas y los cálculos están en la API:
 * acá solo se arma la solicitud a partir del formulario.
 */
@Service
public class ServicioPromociones {

    private final ClienteApi clienteApi;

    public ServicioPromociones(ClienteApi clienteApi) {
        this.clienteApi = clienteApi;
    }

    public List<PromocionDTO> listar(String token) {
        return clienteApi.listar(token, PromocionDTO[].class, "/api/promociones");
    }

    public PromocionDTO obtener(String token, String id) {
        return clienteApi.obtener(token, PromocionDTO.class, "/api/promociones/{id}", id);
    }

    public PromocionDTO crear(String token, PromocionFormularioDTO f) {
        return clienteApi.enviar(token, solicitud(f), PromocionDTO.class, "/api/promociones");
    }

    public PromocionDTO modificar(String token, String id, PromocionFormularioDTO f) {
        return clienteApi.reemplazar(token, solicitud(f), PromocionDTO.class, "/api/promociones/{id}", id);
    }

    /** Activa o desactiva sin cambiar ningún otro dato. */
    public PromocionDTO cambiarActiva(String token, String id, boolean activa) {
        PromocionFormularioDTO f = PromocionFormularioDTO.desde(obtener(token, id));
        f.setActiva(activa);
        return modificar(token, id, f);
    }

    public void eliminar(String token, String id) {
        clienteApi.eliminar(token, "/api/promociones/{id}", id);
    }

    /** Productos activos para elegir el alcance, ordenados por nombre. */
    public List<ProductoDTO> productos(String token) {
        return clienteApi.listar(token, ProductoDTO[].class, "/api/productos").stream()
                .sorted(Comparator.comparing(ProductoDTO::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public List<CategoriaDTO> subcategorias(String token) {
        return clienteApi.listar(token, CategoriaDTO[].class, "/api/subcategorias").stream()
                .sorted(Comparator.comparing(CategoriaDTO::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private static PromocionSolicitudDTO solicitud(PromocionFormularioDTO f) {
        boolean codigo = "CODIGO".equals(f.getTipo());
        List<PromocionSolicitudDTO.Tramo> tramos = new ArrayList<>();
        if ("VOLUMEN".equals(f.getTipo())) {
            for (int i = 0; i < f.getTramoDesde().size(); i++) {
                Integer desde = f.desde(i);
                if (desde == null && f.porcentajeTramo(i) == null) {
                    continue;   // renglón vacío
                }
                tramos.add(new PromocionSolicitudDTO.Tramo(desde, f.hasta(i), f.porcentajeTramo(i)));
            }
        }
        return new PromocionSolicitudDTO(f.getTipo(), f.getNombre(), f.getDescripcion(), fecha(f.getFechaInicio()),
                fecha(f.getFechaFin()), f.isActiva(), f.getIdsProductos(), f.getIdsSubCategorias(),
                "PORCENTAJE_PRODUCTO".equals(f.getTipo()) ? f.getPorcentaje() : null, tramos,
                codigo ? f.getCodigo() : null, codigo ? f.getTipoValor() : null, codigo ? f.getValor() : null,
                codigo ? f.getMontoMinimo() : null, codigo ? f.getUsosMaximos() : null, codigo ? f.isAcumulable() : null);
    }

    private static LocalDate fecha(String texto) {
        try {
            return texto == null || texto.isBlank() ? null : LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
