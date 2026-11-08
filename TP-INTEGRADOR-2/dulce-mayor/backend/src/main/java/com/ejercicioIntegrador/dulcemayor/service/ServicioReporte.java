package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FilaReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoSerieDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.model.LineaPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Reportes y estadísticas (Fase 9). Trabaja sobre los pedidos que ya comprometieron stock
 * (confirmados, completados o entregados) dentro de un período inclusivo.
 *
 * <p>Agrupaciones soportadas: {@code departamento} (ubicación del punto de venta),
 * {@code preventista}, {@code cliente} (comercio) y {@code estado}.</p>
 */
@Service
public class ServicioReporte {

    /** Estados que representan una venta real para los reportes. */
    private static final List<EstadoPedido> ESTADOS_VENTA =
            List.of(EstadoPedido.CONFIRMADO, EstadoPedido.COMPLETADO, EstadoPedido.ENTREGADO);

    private final RepositorioPedido repositorioPedido;
    private final RepositorioStock repositorioStock;

    public ServicioReporte(RepositorioPedido repositorioPedido, RepositorioStock repositorioStock) {
        this.repositorioPedido = repositorioPedido;
        this.repositorioStock = repositorioStock;
    }

    @Transactional(readOnly = true)
    public ReporteVentasDTO ventas(LocalDate desde, LocalDate hasta, String agrupacion) {
        Periodo periodo = Periodo.de(desde, hasta);
        List<Pedido> pedidos = enPeriodo(periodo);
        Function<Pedido, String> clave = agrupador(agrupacion);

        Map<String, FilaReporteDTO> acumulado = new LinkedHashMap<>();
        long presentaciones = 0;
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Pedido pedido : pedidos) {
            String grupo = clave.apply(pedido);
            long unidades = pedido.getLineas() == null ? 0
                    : pedido.getLineas().stream().mapToLong(LineaPedido::getCantidadPresentaciones).sum();
            BigDecimal importe = pedido.getTotal() == null ? BigDecimal.ZERO : pedido.getTotal();
            presentaciones += unidades;
            total = total.add(importe);
            FilaReporteDTO actual = acumulado.get(grupo);
            acumulado.put(grupo, new FilaReporteDTO(grupo,
                    (actual == null ? 0 : actual.pedidos()) + 1,
                    (actual == null ? 0 : actual.presentaciones()) + unidades,
                    (actual == null ? BigDecimal.ZERO : actual.importe()).add(importe)));
            porEstado.merge(pedido.getEstado().name(), 1L, Long::sum);
        }
        List<FilaReporteDTO> filas = new ArrayList<>(acumulado.values());
        filas.sort(Comparator.comparing(FilaReporteDTO::importe).reversed());
        return new ReporteVentasDTO(periodo.desde(), periodo.hasta(),
                agrupacion == null ? "departamento" : agrupacion.toLowerCase(java.util.Locale.ROOT),
                pedidos.size(), presentaciones, escalar(total), filas, porEstado);
    }

    /** Serie temporal por período y por preventista, útil para el gráfico mensual. */
    @Transactional(readOnly = true)
    public List<PuntoSerieDTO> pedidosPorPreventista(LocalDate desde, LocalDate hasta, boolean porMes) {
        Periodo periodo = Periodo.de(desde, hasta);
        Map<String, PuntoSerieDTO> serie = new LinkedHashMap<>();
        DateTimeFormatter formato = porMes ? DateTimeFormatter.ofPattern("yyyy-MM") : DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Pedido pedido : enPeriodo(periodo)) {
            LocalDateTime fecha = pedido.getFechaConfirmacion() != null ? pedido.getFechaConfirmacion()
                    : pedido.getFechaCreacion();
            String periodoClave = fecha == null ? "sin-fecha" : formato.format(fecha);
            String preventista = nombrePreventista(pedido);
            String clave = periodoClave + "|" + preventista;
            BigDecimal importe = pedido.getTotal() == null ? BigDecimal.ZERO : pedido.getTotal();
            PuntoSerieDTO actual = serie.get(clave);
            serie.put(clave, new PuntoSerieDTO(periodoClave, preventista,
                    (actual == null ? 0 : actual.cantidad()) + 1,
                    (actual == null ? BigDecimal.ZERO : actual.importe()).add(importe)));
        }
        return serie.values().stream()
                .sorted(Comparator.comparing(PuntoSerieDTO::periodo).thenComparing(PuntoSerieDTO::serie))
                .toList();
    }

    /** Cantidades en stock por producto, con el mínimo para marcar los que están bajos. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> stockPorProducto() {
        return repositorioStock.findByEliminadoFalse().stream()
                .sorted(Comparator.comparing(stock -> stock.getProducto().getNombre(),
                        String.CASE_INSENSITIVE_ORDER))
                .map(stock -> (Map<String, Object>) new LinkedHashMap<String, Object>(Map.of(
                        "producto", stock.getProducto().getNombre(),
                        "codigo", stock.getProducto().getCodigo(),
                        "cantidadActual", stock.getCantidadActual(),
                        "stockMinimo", stock.getStockMinimo(),
                        "bajoMinimo", stock.getCantidadActual() <= stock.getStockMinimo())))
                .toList();
    }

    // ------------------------------------------------------------------ auxiliares

    private List<Pedido> enPeriodo(Periodo periodo) {
        return repositorioPedido.findByEstadoInAndEliminadoFalse(ESTADOS_VENTA).stream()
                .filter(pedido -> {
                    LocalDateTime fecha = pedido.getFechaConfirmacion() != null ? pedido.getFechaConfirmacion()
                            : pedido.getFechaCreacion();
                    if (fecha == null) {
                        return false;
                    }
                    LocalDate dia = fecha.toLocalDate();
                    return !dia.isBefore(periodo.desde()) && !dia.isAfter(periodo.hasta());
                })
                .toList();
    }

    private static Function<Pedido, String> agrupador(String agrupacion) {
        String criterio = agrupacion == null ? "departamento" : agrupacion.toLowerCase(java.util.Locale.ROOT);
        return switch (criterio) {
            case "preventista" -> ServicioReporte::nombrePreventista;
            case "cliente" -> pedido -> pedido.getPuntoVenta().getNombreComercial();
            case "estado" -> pedido -> pedido.getEstado().name();
            default -> pedido -> {
                var direccion = pedido.getPuntoVenta().getDireccion();
                if (direccion == null || direccion.getLocalidad() == null
                        || direccion.getLocalidad().getDepartamento() == null) {
                    return "Sin departamento";
                }
                return direccion.getLocalidad().getDepartamento().getNombre();
            };
        };
    }

    private static String nombrePreventista(Pedido pedido) {
        if (pedido.getPreventista() == null) {
            return "Sin preventista";
        }
        String nombre = ((pedido.getPreventista().getNombre() == null ? "" : pedido.getPreventista().getNombre())
                + " " + (pedido.getPreventista().getApellido() == null ? "" : pedido.getPreventista().getApellido()))
                .trim();
        return nombre.isEmpty() ? "Sin preventista" : nombre;
    }

    private static BigDecimal escalar(BigDecimal valor) {
        return valor.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** Período inclusivo; si faltan los extremos se usa el mes en curso. */
    private record Periodo(LocalDate desde, LocalDate hasta) {

        static Periodo de(LocalDate desde, LocalDate hasta) {
            LocalDate fin = hasta == null ? LocalDate.now() : hasta;
            LocalDate inicio = desde == null ? fin.withDayOfMonth(1) : desde;
            if (inicio.isAfter(fin)) {
                LocalDate temporal = inicio;
                inicio = fin;
                fin = temporal;
            }
            return new Periodo(inicio, fin);
        }
    }
}
