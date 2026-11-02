package com.dulcemayor.admin.service;

import com.dulcemayor.admin.dto.ClienteDTO;
import com.dulcemayor.admin.dto.ConteoRolDTO;
import com.dulcemayor.admin.dto.EmpleadoDTO;
import com.dulcemayor.admin.dto.IndicadorDTO;
import com.dulcemayor.admin.dto.PanelDTO;
import com.dulcemayor.admin.dto.ProductoCatalogoDTO;
import com.dulcemayor.admin.dto.Roles;
import com.dulcemayor.admin.dto.SegmentoDTO;
import com.dulcemayor.admin.dto.StockDTO;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Arma el panel de inicio con datos reales de los módulos que ya existen (Catálogo y Stock, Personas).
 * No inventa cifras: los módulos que todavía no están implementados (pedidos, rutas, promociones,
 * facturación) se muestran como pendientes en la vista, sin valores.
 */
@Service
public class ServicioPanel {

    private static final String[] COLORES = {"#d10f8c", "#1f6feb", "#7a35e0", "#ffc52e", "#16935a", "#ff7ac6", "#5aa2ff", "#b38cf2"};
    private static final int MAXIMO_SEGMENTOS = COLORES.length;

    private final ServicioCatalogo servicioCatalogo;
    private final ServicioClientes servicioClientes;
    private final ServicioEmpleados servicioEmpleados;

    public ServicioPanel(ServicioCatalogo servicioCatalogo, ServicioClientes servicioClientes,
                         ServicioEmpleados servicioEmpleados) {
        this.servicioCatalogo = servicioCatalogo;
        this.servicioClientes = servicioClientes;
        this.servicioEmpleados = servicioEmpleados;
    }

    public PanelDTO armar(SesionUsuario sesion) {
        String token = sesion.getToken();
        List<IndicadorDTO> indicadores = new ArrayList<>();
        List<SegmentoDTO> categorias = List.of();
        long publicados = 0;
        List<StockDTO> stockCritico = List.of();
        List<ConteoRolDTO> personal = List.of();

        if (sesion.isPuedeVerCatalogo()) {
            List<ProductoCatalogoDTO> catalogo = servicioCatalogo.listarProductos(token, null, null, false);
            publicados = catalogo.size();
            categorias = segmentosPorCategoria(catalogo);
            String detalle = "Con precio vigente";
            if (sesion.isAdministrativo()) {
                detalle = "De " + servicioCatalogo.listarProductosActivos(token).size() + " productos activos";
            }
            indicadores.add(new IndicadorDTO("Productos publicados", String.valueOf(publicados), detalle,
                    "i-catalogo", "tono-magenta", "/catalogo"));
            if (!sesion.isJefe()) {
                long ofertas = catalogo.stream().filter(ProductoCatalogoDTO::isEnOferta).count();
                indicadores.add(new IndicadorDTO("En oferta", String.valueOf(ofertas),
                        "Productos marcados como oferta", "i-etiqueta", "tono-amarillo", "/catalogo?ofertas=true"));
            }
        }
        if (sesion.isAdministrativo()) {
            List<StockDTO> stock = servicioCatalogo.listarStock(token);
            long bajoMinimo = stock.stream().filter(StockDTO::isBajoMinimo).count();
            long sinStock = stock.stream().filter(s -> s.getCantidadActual() <= 0).count();
            stockCritico = stock.stream()
                    .filter(s -> s.isBajoMinimo() || s.getCantidadActual() <= 0)
                    .sorted(Comparator.comparingInt(StockDTO::getCantidadActual))
                    .limit(6)
                    .toList();
            indicadores.add(new IndicadorDTO("Alertas de stock", String.valueOf(bajoMinimo),
                    sinStock + " sin stock", "i-alerta", bajoMinimo > 0 ? "tono-rojo" : "tono-verde", null));
            List<ClienteDTO> clientes = servicioClientes.listar(token);
            indicadores.add(new IndicadorDTO("Clientes registrados", String.valueOf(clientes.size()),
                    "Desde el sitio institucional", "i-tienda", "tono-violeta", "/clientes"));
        }
        if (sesion.isJefe()) {
            List<EmpleadoDTO> empleados = servicioEmpleados.listar(token, null);
            personal = conteoPorRol(empleados);
            long pendientes = empleados.stream().filter(EmpleadoDTO::isDebeCambiarClave).count();
            indicadores.add(new IndicadorDTO("Empleados activos", String.valueOf(empleados.size()),
                    pendientes + " con clave temporal pendiente", "i-personas", "tono-azul", "/empleados"));
        }
        return new PanelDTO(indicadores, categorias, fondoDona(categorias), publicados, stockCritico, personal);
    }

    private static List<SegmentoDTO> segmentosPorCategoria(List<ProductoCatalogoDTO> catalogo) {
        Map<String, Long> porCategoria = catalogo.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria() == null ? "Sin categoría" : p.getCategoria(),
                        LinkedHashMap::new, Collectors.counting()));
        List<Map.Entry<String, Long>> ordenadas = porCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();
        List<SegmentoDTO> segmentos = new ArrayList<>();
        long otros = 0;
        for (int i = 0; i < ordenadas.size(); i++) {
            if (i < MAXIMO_SEGMENTOS - 1 || ordenadas.size() == MAXIMO_SEGMENTOS) {
                segmentos.add(new SegmentoDTO(ordenadas.get(i).getKey(), ordenadas.get(i).getValue(), COLORES[i]));
            } else {
                otros += ordenadas.get(i).getValue();
            }
        }
        if (otros > 0) {
            segmentos.add(new SegmentoDTO("Otras", otros, COLORES[MAXIMO_SEGMENTOS - 1]));
        }
        return segmentos;
    }

    /** Fondo CSS conic-gradient de la dona; se arma acá porque solo depende de los datos. */
    private static String fondoDona(List<SegmentoDTO> segmentos) {
        long total = segmentos.stream().mapToLong(SegmentoDTO::cantidad).sum();
        if (total == 0) {
            return "background: var(--dm-superficie-2)";
        }
        StringBuilder css = new StringBuilder("background: conic-gradient(");
        double acumulado = 0;
        for (int i = 0; i < segmentos.size(); i++) {
            double desde = acumulado;
            acumulado += 100.0 * segmentos.get(i).cantidad() / total;
            css.append(i == 0 ? "" : ", ").append(segmentos.get(i).color()).append(' ')
                    .append(String.format(java.util.Locale.ROOT, "%.2f%% %.2f%%", desde, acumulado));
        }
        return css.append(')').toString();
    }

    private static List<ConteoRolDTO> conteoPorRol(List<EmpleadoDTO> empleados) {
        Map<String, Long> porRol = empleados.stream()
                .collect(Collectors.groupingBy(EmpleadoDTO::getRol, Collectors.counting()));
        long maximo = porRol.values().stream().mapToLong(Long::longValue).max().orElse(1);
        return List.of(Roles.JEFE, Roles.ADMINISTRATIVO, Roles.PREVENTISTA, Roles.DISTRIBUIDOR).stream()
                .map(rol -> {
                    long cantidad = porRol.getOrDefault(rol, 0L);
                    return new ConteoRolDTO(rol, cantidad, (int) Math.round(100.0 * cantidad / Math.max(maximo, 1)));
                })
                .toList();
    }
}
