package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ArchivoReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.FiltroReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubtotalVentasDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VentaDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.exportacion.ExportadorReportes;
import com.ejercicioIntegrador.dulcemayor.exportacion.FormatoExportacion;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Departamento;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes administrativos: clientes y pedidos, e histórico de ventas por departamento y preventista.
 *
 * Aquí viven TODAS las reglas de los reportes:
 *  - validación de filtros (la fecha de inicio no puede ser posterior a la de fin; el histórico exige período);
 *  - intervalo inclusivo: desde 00:00 del primer día hasta el final del último día, sin registros fuera de él;
 *  - qué es una "venta": un pedido confirmado por el cliente (tiene fecha de facturación) y no anulado, ubicado
 *    en el período por su fecha de facturación; el reporte de clientes y pedidos, en cambio, ubica los pedidos
 *    por su fecha de carga e incluye todos los estados;
 *  - monto facturado: el total del pedido solo si fue confirmado y no anulado;
 *  - estado de envío: se deriva del estado del pedido y de si tiene distribuidor asignado;
 *  - subtotales y totales.
 *
 * No hereda de BaseService porque no administra una entidad propia (mismo criterio que ServicioCatalogo).
 * No accede a ningún repositorio: los pedidos los pide a ServicioPedido, el departamento a ServicioDepartamento
 * y los empleados a ServicioEmpleado. El archivo lo genera la estrategia de exportación elegida por
 * ExportadorReportes; este servicio nunca pregunta por el formato.
 */
@Service
public class ServicioReporte {

    /** Cota inferior técnica cuando no se indica fecha de inicio: anterior a cualquier pedido del sistema. */
    private static final LocalDate SIN_LIMITE_INFERIOR = LocalDate.of(2000, 1, 1);

    static final String ENVIO_NO_CONFIRMADO = "No confirmado";
    static final String ENVIO_ANULADO = "Anulado";
    static final String ENVIO_PENDIENTE_DE_ASIGNACION = "Pendiente de asignación";
    static final String ENVIO_EN_REPARTO = "En reparto";
    static final String ENVIO_ENTREGADO = "Entregado";

    private final ServicioPedido servicioPedido;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioDepartamento servicioDepartamento;
    private final ExportadorReportes exportadorReportes;

    public ServicioReporte(ServicioPedido servicioPedido, ServicioEmpleado servicioEmpleado,
                           ServicioDepartamento servicioDepartamento, ExportadorReportes exportadorReportes) {
        this.servicioPedido = servicioPedido;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioDepartamento = servicioDepartamento;
        this.exportadorReportes = exportadorReportes;
    }

    // ------------------------------------------------------------------
    // Reporte general de clientes y pedidos
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReporteClientesPedidosDTO generarReporteClientesPedidos(FiltroReporteDTO filtro, String correoUsuario)
            throws MiException {
        FiltroReporteDTO criterios = filtro == null ? new FiltroReporteDTO(null, null, null, null, null) : filtro;
        validarPeriodo(criterios.desde(), criterios.hasta(), false);
        Departamento departamento = resolverDepartamento(criterios.idDepartamento());

        LocalDateTime inicio = (criterios.desde() == null ? SIN_LIMITE_INFERIOR : criterios.desde()).atStartOfDay();
        LocalDateTime finExclusivo = (criterios.hasta() == null ? LocalDate.now() : criterios.hasta())
                .plusDays(1).atStartOfDay();

        List<PedidoReporteDTO> filas = new ArrayList<>();
        for (Pedido pedido : servicioPedido.listarEntidadesCargadasEntre(inicio, finExclusivo)) {
            if (cumpleDepartamento(pedido, departamento)
                    && (criterios.estado() == null || pedido.getEstado() == criterios.estado())) {
                filas.add(armarFila(pedido));
            }
        }
        filas.sort(Comparator.comparing(PedidoReporteDTO::nombreCliente, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(PedidoReporteDTO::nombreComercio, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(PedidoReporteDTO::idPuntoVenta)
                .thenComparing(PedidoReporteDTO::fechaHoraCarga));

        long comercios = filas.stream().map(PedidoReporteDTO::idPuntoVenta).distinct().count();
        long entregados = filas.stream().filter(f -> f.estado() == EstadoPedido.ENTREGADO).count();
        long pendientes = filas.stream().filter(f -> f.estado() == EstadoPedido.PENDIENTE_DE_ENTREGA).count();
        double totalFacturado = redondear(filas.stream()
                .filter(f -> f.montoFacturado() != null).mapToDouble(PedidoReporteDTO::montoFacturado).sum());

        return new ReporteClientesPedidosDTO(criterios.desde(), criterios.hasta(),
                departamento == null ? null : departamento.getNombre(), criterios.estado(),
                criterios.estado() == null ? null : descripcionEstado(criterios.estado()), LocalDateTime.now(),
                nombreDelUsuario(correoUsuario), filas, comercios, filas.size(), entregados, pendientes,
                totalFacturado);
    }

    @Transactional(readOnly = true)
    public ArchivoReporteDTO exportarClientesPedidos(FiltroReporteDTO filtro, FormatoExportacion formato,
                                                     String correoUsuario) throws MiException {
        ReporteClientesPedidosDTO reporte = generarReporteClientesPedidos(filtro, correoUsuario);
        byte[] contenido = exportadorReportes.exportar(reporte, formato);
        return new ArchivoReporteDTO("reporte-clientes-pedidos-" + LocalDate.now() + "." + formato.getExtension(),
                formato.getTipoContenido(), contenido);
    }

    private PedidoReporteDTO armarFila(Pedido pedido) {
        PuntoVenta puntoVenta = pedido.getPuntoVenta();
        Cliente cliente = puntoVenta.getCliente();
        boolean facturado = pedido.isConfirmado() && pedido.getEstado() != EstadoPedido.ANULADO;
        return new PedidoReporteDTO(puntoVenta.getId(), cliente.getId(), cliente.getNumeroDocumento(),
                nombreCompleto(cliente.getNombre(), cliente.getApellido()), correoDe(cliente),
                puntoVenta.getNombreFantasia(), nombreDepartamento(puntoVenta),
                ServicioPuntoVenta.textoDireccion(puntoVenta), pedido.getNumeroPedido(), pedido.getFechaHoraCarga(),
                nombreCompleto(pedido.getPreventista().getNombre(), pedido.getPreventista().getApellido()),
                pedido.getEstado(), descripcionEstado(pedido.getEstado()), descripcionEnvio(pedido),
                pedido.getDistribuidor() == null ? null
                        : nombreCompleto(pedido.getDistribuidor().getNombre(), pedido.getDistribuidor().getApellido()),
                pedido.getFechaFacturacion(), pedido.getFechaEntrega(), facturado ? pedido.getTotal() : null);
    }

    // ------------------------------------------------------------------
    // Histórico de ventas por departamento y preventista
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReporteVentasDTO generarReporteVentas(FiltroReporteDTO filtro, String correoUsuario) throws MiException {
        FiltroReporteDTO criterios = filtro == null ? new FiltroReporteDTO(null, null, null, null, null) : filtro;
        validarPeriodo(criterios.desde(), criterios.hasta(), true);
        Departamento departamento = resolverDepartamento(criterios.idDepartamento());
        Preventista preventista = resolverPreventista(criterios.idPreventista());

        LocalDateTime inicio = criterios.desde().atStartOfDay();
        LocalDateTime finExclusivo = criterios.hasta().plusDays(1).atStartOfDay();

        List<VentaDTO> ventas = new ArrayList<>();
        for (Pedido pedido : servicioPedido.listarEntidadesFacturadasEntre(inicio, finExclusivo)) {
            if (pedido.getEstado() != EstadoPedido.ANULADO
                    && cumpleDepartamento(pedido, departamento)
                    && (preventista == null || pedido.getPreventista().getId().equals(preventista.getId()))) {
                ventas.add(armarVenta(pedido));
            }
        }
        ventas.sort(Comparator.comparing(VentaDTO::departamento, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(VentaDTO::preventista, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(VentaDTO::fechaVenta)
                .thenComparingLong(VentaDTO::numeroPedido));

        long clientes = ventas.stream().map(VentaDTO::idCliente).distinct().count();
        double totalGeneral = redondear(ventas.stream().mapToDouble(VentaDTO::total).sum());

        return new ReporteVentasDTO(criterios.desde(), criterios.hasta(),
                departamento == null ? null : departamento.getNombre(),
                preventista == null ? null : nombreCompleto(preventista.getNombre(), preventista.getApellido()),
                LocalDateTime.now(), nombreDelUsuario(correoUsuario), ventas,
                agrupar(ventas, true, false), agrupar(ventas, false, true), agrupar(ventas, true, true),
                ventas.size(), clientes, totalGeneral);
    }

    @Transactional(readOnly = true)
    public ArchivoReporteDTO exportarVentas(FiltroReporteDTO filtro, FormatoExportacion formato,
                                            String correoUsuario) throws MiException {
        ReporteVentasDTO reporte = generarReporteVentas(filtro, correoUsuario);
        byte[] contenido = exportadorReportes.exportar(reporte, formato);
        return new ArchivoReporteDTO("historico-ventas-" + reporte.desde() + "-a-" + reporte.hasta() + "."
                + formato.getExtension(), formato.getTipoContenido(), contenido);
    }

    private VentaDTO armarVenta(Pedido pedido) {
        PuntoVenta puntoVenta = pedido.getPuntoVenta();
        Cliente cliente = puntoVenta.getCliente();
        Departamento departamento = puntoVenta.getDireccion().getLocalidad().getDepartamento();
        Preventista preventista = pedido.getPreventista();
        return new VentaDTO(departamento.getId(), departamento.getNombre(), preventista.getId(),
                nombreCompleto(preventista.getNombre(), preventista.getApellido()), cliente.getId(),
                cliente.getNumeroDocumento(), nombreCompleto(cliente.getNombre(), cliente.getApellido()),
                puntoVenta.getNombreFantasia(), pedido.getNumeroPedido(), pedido.getFechaFacturacion(),
                pedido.getSubtotal(), pedido.getDescuento(), pedido.getTotal());
    }

    /** Subtotales por departamento, por preventista o por ambos, ordenados alfabéticamente. */
    private static List<SubtotalVentasDTO> agrupar(List<VentaDTO> ventas, boolean porDepartamento,
                                                   boolean porPreventista) {
        Map<String, Acumulado> acumulados = new LinkedHashMap<>();
        for (VentaDTO venta : ventas) {
            String clave = (porDepartamento ? venta.idDepartamento() : "") + "|"
                    + (porPreventista ? venta.idPreventista() : "");
            Acumulado acumulado = acumulados.computeIfAbsent(clave, k -> new Acumulado(
                    porDepartamento ? venta.departamento() : null, porPreventista ? venta.preventista() : null));
            acumulado.cantidad++;
            acumulado.total += venta.total();
        }
        Comparator<String> orden = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
        return acumulados.values().stream()
                .map(a -> new SubtotalVentasDTO(a.departamento, a.preventista, a.cantidad, redondear(a.total)))
                .sorted(Comparator.comparing(SubtotalVentasDTO::departamento, orden)
                        .thenComparing(SubtotalVentasDTO::preventista, orden))
                .toList();
    }

    private static final class Acumulado {
        private final String departamento;
        private final String preventista;
        private long cantidad;
        private double total;

        private Acumulado(String departamento, String preventista) {
            this.departamento = departamento;
            this.preventista = preventista;
        }
    }

    // ------------------------------------------------------------------
    // Reglas y utilidades compartidas
    // ------------------------------------------------------------------

    /** El histórico exige ambas fechas; en cualquier caso la de inicio no puede ser posterior a la de fin. */
    private static void validarPeriodo(LocalDate desde, LocalDate hasta, boolean obligatorio) throws MiException {
        if (obligatorio && (desde == null || hasta == null)) {
            throw new MiException("Debe indicar la fecha de inicio y la fecha de finalización del período");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new MiException("La fecha de inicio no puede ser posterior a la fecha de finalización");
        }
    }

    private Departamento resolverDepartamento(String idDepartamento) throws MiException {
        if (idDepartamento == null || idDepartamento.isBlank()) {
            return null;
        }
        try {
            return servicioDepartamento.obtenerEntidad(idDepartamento);
        } catch (RecursoNoEncontradoException e) {
            throw new MiException("No se encontró el departamento indicado");
        }
    }

    private Preventista resolverPreventista(String idPreventista) throws MiException {
        if (idPreventista == null || idPreventista.isBlank()) {
            return null;
        }
        Empleado empleado;
        try {
            empleado = servicioEmpleado.obtenerEntidad(idPreventista);
        } catch (RecursoNoEncontradoException e) {
            throw new MiException("No se encontró el preventista indicado");
        }
        if (!(empleado instanceof Preventista preventista)) {
            throw new MiException("El empleado indicado no es un preventista");
        }
        return preventista;
    }

    private static boolean cumpleDepartamento(Pedido pedido, Departamento departamento) {
        return departamento == null || departamento.getId().equals(
                pedido.getPuntoVenta().getDireccion().getLocalidad().getDepartamento().getId());
    }

    /** El estado de envío resume el estado del pedido y si ya tiene distribuidor asignado. */
    static String descripcionEnvio(Pedido pedido) {
        return switch (pedido.getEstado()) {
            case CARGADO -> ENVIO_NO_CONFIRMADO;
            case ANULADO -> ENVIO_ANULADO;
            case PENDIENTE_DE_ENTREGA -> pedido.getDistribuidor() == null
                    ? ENVIO_PENDIENTE_DE_ASIGNACION : ENVIO_EN_REPARTO;
            case ENTREGADO -> ENVIO_ENTREGADO;
        };
    }

    static String descripcionEstado(EstadoPedido estado) {
        return switch (estado) {
            case CARGADO -> "Cargado";
            case PENDIENTE_DE_ENTREGA -> "Pendiente de entrega";
            case ENTREGADO -> "Entregado";
            case ANULADO -> "Anulado";
        };
    }

    private static String nombreDepartamento(PuntoVenta puntoVenta) {
        return puntoVenta.getDireccion().getLocalidad().getDepartamento().getNombre();
    }

    private static String correoDe(Cliente cliente) {
        return cliente.getUsuario() == null ? null : cliente.getUsuario().getNombreUsuario();
    }

    private static String nombreCompleto(String nombre, String apellido) {
        return nombre + " " + apellido;
    }

    /** Nombre del empleado que genera el reporte; si no se lo encuentra se muestra su correo. */
    private String nombreDelUsuario(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return null;
        }
        try {
            Empleado empleado = servicioEmpleado.obtenerEmpleadoPorCorreo(correoUsuario);
            return nombreCompleto(empleado.getNombre(), empleado.getApellido());
        } catch (MiException e) {
            return correoUsuario;
        }
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
