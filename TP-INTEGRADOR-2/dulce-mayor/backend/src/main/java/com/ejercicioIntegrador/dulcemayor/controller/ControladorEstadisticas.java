package com.ejercicioIntegrador.dulcemayor.controller;

import com.ejercicioIntegrador.dulcemayor.dto.PedidosPorPreventistaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockCategoriaDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Datos de los gráficos del panel (HU-43). La agregación se hace en el backend; el frontend solo dibuja.
 * Permisos (SecurityConfig): stock por categoría → ADMINISTRATIVO (y JEFE); pedidos por preventista → solo JEFE.
 */
@RestController
@RequestMapping("/api/estadisticas")
public class ControladorEstadisticas {

    private final ServicioStock servicioStock;
    private final ServicioPedido servicioPedido;

    public ControladorEstadisticas(ServicioStock servicioStock, ServicioPedido servicioPedido) {
        this.servicioStock = servicioStock;
        this.servicioPedido = servicioPedido;
    }

    @GetMapping("/stock-por-categoria")
    public ResponseEntity<List<StockCategoriaDTO>> stockPorCategoria() {
        return ResponseEntity.ok(servicioStock.stockPorCategoria());
    }

    /** mes con formato AAAA-MM (por defecto, el mes actual). */
    @GetMapping("/pedidos-por-preventista")
    public ResponseEntity<PedidosPorPreventistaDTO> pedidosPorPreventista(@RequestParam(required = false) String mes)
            throws MiException {
        YearMonth periodo;
        try {
            periodo = mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes.trim());
        } catch (DateTimeParseException e) {
            throw new MiException("El mes debe tener el formato AAAA-MM");
        }
        return ResponseEntity.ok(servicioPedido.pedidosPorPreventistaYDia(periodo));
    }
}
