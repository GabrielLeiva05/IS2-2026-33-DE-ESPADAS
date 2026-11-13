package com.dulcemayor.admin.controller;

import com.dulcemayor.admin.dto.PedidosPorPreventistaDTO;
import com.dulcemayor.admin.dto.StockCategoriaDTO;
import com.dulcemayor.admin.seguridad.GestorSesion;
import com.dulcemayor.admin.seguridad.SesionUsuario;
import com.dulcemayor.admin.service.ServicioEstadisticas;
import com.dulcemayor.admin.service.ServicioPanel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Panel de inicio. Gráficos (HU-43): torta del stock por categoría para ADMINISTRATIVO y JEFE; barras de pedidos
 * por preventista por día solo para el JEFE (decisión del equipo). La API vuelve a exigir esos roles.
 */
@Controller
public class ControladorInicio {

    private final ServicioPanel servicioPanel;
    private final ServicioEstadisticas servicioEstadisticas;
    private final GestorSesion gestorSesion;

    public ControladorInicio(ServicioPanel servicioPanel, ServicioEstadisticas servicioEstadisticas,
                             GestorSesion gestorSesion) {
        this.servicioPanel = servicioPanel;
        this.servicioEstadisticas = servicioEstadisticas;
        this.gestorSesion = gestorSesion;
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String mes, HttpServletRequest request, Model model) {
        SesionUsuario sesion = gestorSesion.actual(request).orElseThrow();
        model.addAttribute("panel", servicioPanel.armar(sesion));
        if (sesion.isAdministrativo()) {
            List<StockCategoriaDTO> stock = servicioEstadisticas.stockPorCategoria(sesion.getToken());
            model.addAttribute("stockPorCategoria", stock);
            model.addAttribute("datosStock", aDatos(stock));
        }
        if (sesion.isJefe()) {
            String periodo = mesValido(mes);
            PedidosPorPreventistaDTO pedidos = servicioEstadisticas.pedidosPorPreventista(sesion.getToken(), periodo);
            model.addAttribute("mes", periodo);
            model.addAttribute("pedidosPorPreventista", pedidos);
            // Para el script del gráfico: mapas y listas (los serializa el inlining de JavaScript de Thymeleaf)
            model.addAttribute("datosPedidos", Map.of("mes", pedidos.mes(), "dias", pedidos.dias(),
                    "series", pedidos.series().stream().map(serie -> Map.of("preventista", serie.preventista(),
                            "cantidades", serie.cantidades(), "total", serie.total())).toList()));
        }
        return "inicio";
    }

    /** Datos actuales de la torta: el panel los vuelve a pedir cada minuto para reflejar cambios de stock. */
    @GetMapping("/estadisticas/stock")
    @ResponseBody
    public ResponseEntity<List<StockCategoriaDTO>> stock(HttpServletRequest request) {
        SesionUsuario sesion = gestorSesion.actual(request).orElseThrow();
        if (!sesion.isAdministrativo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(servicioEstadisticas.stockPorCategoria(sesion.getToken()));
    }

    private static List<Map<String, Object>> aDatos(List<StockCategoriaDTO> stock) {
        return stock.stream().map(s -> Map.<String, Object>of("categoria", s.categoria(), "bultos", s.bultos(),
                "productos", s.productos())).toList();
    }

    private static String mesValido(String mes) {
        try {
            return mes == null || mes.isBlank() ? YearMonth.now().toString() : YearMonth.parse(mes.trim()).toString();
        } catch (RuntimeException e) {
            return YearMonth.now().toString();
        }
    }
}
