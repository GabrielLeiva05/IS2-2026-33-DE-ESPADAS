package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.FiltroReporteDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteClientesPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReporteVentasDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exportacion.ContextoExportacion;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.model.Departamento;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Localidad;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de los reportes: validación de fechas, intervalo inclusivo, qué es una venta, estados y totales. */
class ServicioReporteTest {

    private ServicioPedido servicioPedido;
    private ServicioReporte servicio;
    private Departamento capital;
    private Departamento godoyCruz;
    private Preventista ana;
    private Preventista luis;

    @BeforeEach
    void setUp() {
        servicioPedido = mock(ServicioPedido.class);
        servicio = new ServicioReporte(servicioPedido, mock(ServicioEmpleado.class), mock(ServicioDepartamento.class),
                mock(ContextoExportacion.class));
        capital = departamento("d1", "Capital");
        godoyCruz = departamento("d2", "Godoy Cruz");
        ana = preventista("e1", "Ana", "Paz");
        luis = preventista("e2", "Luis", "Gil");
    }

    private static Departamento departamento(String id, String nombre) {
        Departamento d = new Departamento();
        d.setId(id);
        d.setNombre(nombre);
        return d;
    }

    private static Preventista preventista(String id, String nombre, String apellido) {
        Preventista p = new Preventista();
        p.setId(id);
        p.setNombre(nombre);
        p.setApellido(apellido);
        return p;
    }

    private static PuntoVenta comercio(String id, String idCliente, String apellido, Departamento departamento) {
        Localidad localidad = new Localidad();
        localidad.setNombre("Localidad");
        localidad.setDepartamento(departamento);
        Direccion direccion = new Direccion();
        direccion.setCalle("San Martín");
        direccion.setNumeracion("100");
        direccion.setLocalidad(localidad);
        Cliente cliente = new Cliente();
        cliente.setId(idCliente);
        cliente.setNombre("Cli");
        cliente.setApellido(apellido);
        PuntoVenta pv = new PuntoVenta();
        pv.setId(id);
        pv.setNombreFantasia("Kiosco " + apellido);
        pv.setDireccion(direccion);
        pv.setCliente(cliente);
        return pv;
    }

    private static Pedido pedido(long numero, PuntoVenta pv, Preventista preventista, EstadoPedido estado, double total,
                                 LocalDateTime carga, LocalDateTime facturacion) {
        Pedido p = new Pedido();
        p.setNumeroPedido(numero);
        p.setPuntoVenta(pv);
        p.setPreventista(preventista);
        p.setEstado(estado);
        p.setSubtotal(total);
        p.setTotal(total);
        p.setFechaHoraCarga(carga);
        p.setFechaFacturacion(facturacion);
        return p;
    }

    private static final LocalDateTime D1 = LocalDateTime.of(2026, 10, 10, 9, 0);

    @Test
    void rechazaFechaDeInicioPosteriorALaDeFin() {
        FiltroReporteDTO filtro = new FiltroReporteDTO(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1), null, null, null);
        assertThrows(MiException.class, () -> servicio.generarReporteClientesPedidos(filtro, null));
        assertThrows(MiException.class, () -> servicio.generarReporteVentas(filtro, null));
    }

    @Test
    void elHistoricoDeVentasExigeElPeriodoCompleto() {
        assertThrows(MiException.class, () -> servicio.generarReporteVentas(
                new FiltroReporteDTO(LocalDate.of(2026, 10, 1), null, null, null, null), null));
        assertThrows(MiException.class, () -> servicio.generarReporteVentas(null, null));
    }

    @Test
    void consultaElIntervaloCompletoIncluyendoElUltimoDia() throws MiException {
        when(servicioPedido.listarEntidadesFacturadasEntre(any(), any())).thenReturn(List.of());

        servicio.generarReporteVentas(new FiltroReporteDTO(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                null, null, null), null);

        verify(servicioPedido).listarEntidadesFacturadasEntre(LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void ventasCuentaSoloPedidosConfirmadosYTotalizaPorDepartamentoYPreventista() throws MiException {
        PuntoVenta a = comercio("pv1", "c1", "Perez", capital);
        PuntoVenta b = comercio("pv2", "c2", "Soto", godoyCruz);
        when(servicioPedido.listarEntidadesFacturadasEntre(any(), any())).thenReturn(List.of(
                pedido(1001, a, ana, EstadoPedido.PENDIENTE_DE_ENTREGA, 100.50, D1, D1),
                pedido(1002, a, ana, EstadoPedido.ENTREGADO, 200, D1, D1.plusDays(1)),
                pedido(1003, b, luis, EstadoPedido.ENTREGADO, 50, D1, D1.plusDays(2)),
                pedido(1004, b, luis, EstadoPedido.ANULADO, 999, D1, D1.plusDays(2))));

        ReporteVentasDTO r = servicio.generarReporteVentas(new FiltroReporteDTO(LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), null, null, null), null);

        assertEquals(3, r.cantidadPedidos());
        assertEquals(2, r.cantidadClientes());
        assertEquals(350.50, r.totalGeneral(), 0.001);
        assertEquals(2, r.porDepartamento().size());
        assertEquals("Capital", r.porDepartamento().get(0).departamento());
        assertEquals(300.50, r.porDepartamento().get(0).total(), 0.001);
        assertNull(r.porDepartamento().get(0).preventista());
        assertEquals(2, r.porPreventista().size());
        assertEquals(2, r.porDepartamentoYPreventista().size());
        assertEquals("Capital", r.ventas().get(0).departamento());
    }

    @Test
    void ventasFiltraPorDepartamentoSinConsultarOtroRepositorio() throws MiException {
        PuntoVenta a = comercio("pv1", "c1", "Perez", capital);
        PuntoVenta b = comercio("pv2", "c2", "Soto", godoyCruz);
        ServicioDepartamento servicioDepartamento = mock(ServicioDepartamento.class);
        when(servicioDepartamento.obtenerEntidad("d2")).thenReturn(godoyCruz);
        servicio = new ServicioReporte(servicioPedido, mock(ServicioEmpleado.class), servicioDepartamento,
                mock(ContextoExportacion.class));
        when(servicioPedido.listarEntidadesFacturadasEntre(any(), any())).thenReturn(List.of(
                pedido(1, a, ana, EstadoPedido.ENTREGADO, 10, D1, D1),
                pedido(2, b, luis, EstadoPedido.ENTREGADO, 20, D1, D1)));

        ReporteVentasDTO r = servicio.generarReporteVentas(new FiltroReporteDTO(LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), "d2", null, null), null);

        assertEquals(1, r.cantidadPedidos());
        assertEquals("Godoy Cruz", r.departamentoFiltrado());
        assertEquals(20, r.totalGeneral(), 0.001);
    }

    @Test
    void clientesPedidosDerivaEstadoDeEnvioYMontoFacturado() throws MiException {
        PuntoVenta a = comercio("pv1", "c1", "Perez", capital);
        Pedido cargado = pedido(1, a, ana, EstadoPedido.CARGADO, 10, D1, null);
        Pedido sinAsignar = pedido(2, a, ana, EstadoPedido.PENDIENTE_DE_ENTREGA, 20, D1.plusHours(1), D1.plusHours(2));
        Pedido enReparto = pedido(3, a, ana, EstadoPedido.PENDIENTE_DE_ENTREGA, 30, D1.plusHours(3), D1.plusHours(4));
        enReparto.setDistribuidor(new Distribuidor());
        enReparto.getDistribuidor().setNombre("Dani");
        enReparto.getDistribuidor().setApellido("Ruiz");
        Pedido entregado = pedido(4, a, ana, EstadoPedido.ENTREGADO, 40, D1.plusHours(5), D1.plusHours(6));
        Pedido anulado = pedido(5, a, ana, EstadoPedido.ANULADO, 50, D1.plusHours(7), null);
        when(servicioPedido.listarEntidadesCargadasEntre(any(), any()))
                .thenReturn(List.of(cargado, sinAsignar, enReparto, entregado, anulado));

        ReporteClientesPedidosDTO r = servicio.generarReporteClientesPedidos(null, null);

        assertEquals(5, r.cantidadPedidos());
        assertEquals(1, r.cantidadComercios());
        assertEquals(1, r.cantidadEntregados());
        assertEquals(2, r.cantidadPendientesDeEntrega());
        assertEquals(90, r.totalFacturado(), 0.001);
        assertEquals("No confirmado", r.pedidos().get(0).estadoEnvio());
        assertNull(r.pedidos().get(0).montoFacturado());
        assertEquals("Pendiente de asignación", r.pedidos().get(1).estadoEnvio());
        assertEquals("En reparto", r.pedidos().get(2).estadoEnvio());
        assertEquals("Dani Ruiz", r.pedidos().get(2).distribuidor());
        assertEquals("Entregado", r.pedidos().get(3).estadoEnvio());
        assertEquals("Anulado", r.pedidos().get(4).estadoEnvio());
        assertNull(r.pedidos().get(4).montoFacturado());
    }

    @Test
    void clientesPedidosFiltraPorEstado() throws MiException {
        PuntoVenta a = comercio("pv1", "c1", "Perez", capital);
        when(servicioPedido.listarEntidadesCargadasEntre(any(), any())).thenReturn(List.of(
                pedido(1, a, ana, EstadoPedido.CARGADO, 10, D1, null),
                pedido(2, a, ana, EstadoPedido.ENTREGADO, 20, D1, D1)));

        ReporteClientesPedidosDTO r = servicio.generarReporteClientesPedidos(
                new FiltroReporteDTO(null, null, null, null, EstadoPedido.ENTREGADO), null);

        assertEquals(1, r.cantidadPedidos());
        assertEquals("Entregado", r.descripcionEstadoFiltrado());
    }
}
