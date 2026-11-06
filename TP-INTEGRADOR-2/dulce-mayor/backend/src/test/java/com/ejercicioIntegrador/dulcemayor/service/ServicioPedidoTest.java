package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoLineaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.LineaPedido;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.ReservaStock;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioReservaStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioPedidoTest {

    private static final String CORREO = "preventista@dulcemayor.test";

    private RepositorioPedido repositorioPedido;
    private RepositorioReservaStock repositorioReservaStock;
    private ServicioPuntoVenta servicioPuntoVenta;
    private ServicioProducto servicioProducto;
    private ServicioVigenciaPrecio servicioVigenciaPrecio;
    private ServicioStock servicioStock;
    private ServicioUsuario servicioUsuario;
    private ServicioEmpleado servicioEmpleado;
    private ServicioPedido servicio;

    private Preventista preventista;
    private PuntoVenta puntoVenta;
    private Producto alfajor;

    @BeforeEach
    void setUp() throws MiException {
        repositorioPedido = mock(RepositorioPedido.class);
        repositorioReservaStock = mock(RepositorioReservaStock.class);
        servicioPuntoVenta = mock(ServicioPuntoVenta.class);
        servicioProducto = mock(ServicioProducto.class);
        servicioVigenciaPrecio = mock(ServicioVigenciaPrecio.class);
        servicioStock = mock(ServicioStock.class);
        servicioUsuario = mock(ServicioUsuario.class);
        servicioEmpleado = mock(ServicioEmpleado.class);
        servicio = new ServicioPedido(repositorioPedido, repositorioReservaStock, servicioPuntoVenta, servicioProducto,
                servicioVigenciaPrecio, servicioStock, servicioUsuario, servicioEmpleado);

        preventista = new Preventista();
        preventista.setId("prev-1");
        puntoVenta = new PuntoVenta();
        puntoVenta.setId("pv-1");
        puntoVenta.setNombreComercial("Kiosco Norte");
        puntoVenta.setPreventista(preventista);
        alfajor = Datos.producto("prod-1", "ALF-001", "Alfajor triple", null);

        Usuario cuenta = new Usuario();
        cuenta.setId("usr-1");
        cuenta.setRol(RolUsuario.PREVENTISTA);
        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO)).thenReturn(Optional.of(cuenta));
        when(servicioEmpleado.buscarPorUsuario("usr-1")).thenReturn(Optional.of(preventista));
        when(servicioPuntoVenta.obtenerPuntoPropio("pv-1", CORREO)).thenReturn(puntoVenta);
        when(servicioProducto.obtenerEntidad("prod-1")).thenReturn(alfajor);
        when(repositorioPedido.save(any(Pedido.class))).then(returnsFirstArg());
    }

    @Test
    void elBorradorCongelaElPrecioConDosDecimalesYRedondeoHalfUp() throws MiException {
        when(servicioVigenciaPrecio.obtenerVigenciaVigente("prod-1")).thenReturn(Optional.of(
                Datos.vigencia("vig-1", alfajor, LocalDate.now().minusDays(10), null, 12.505)));

        PedidoResponseDTO creado = servicio.crearBorrador(
                new PedidoRequestDTO("pv-1", List.of(new PedidoLineaRequestDTO("prod-1", 3))), CORREO);

        assertEquals(EstadoPedido.BORRADOR, creado.estado());
        assertEquals(new BigDecimal("12.51"), creado.lineas().get(0).precioUnitario());
        assertEquals(new BigDecimal("37.53"), creado.lineas().get(0).subtotal());
        assertEquals(new BigDecimal("37.53"), creado.total());
        assertEquals("pv-1", creado.idPuntoVenta());
        assertEquals("prev-1", creado.idPreventista());
    }

    @Test
    void elBorradorRechazaUnProductoRepetido() throws MiException {
        when(servicioVigenciaPrecio.obtenerVigenciaVigente("prod-1")).thenReturn(Optional.of(
                Datos.vigencia("vig-1", alfajor, LocalDate.now().minusDays(10), null, 100.0)));

        assertThrows(MiException.class, () -> servicio.crearBorrador(new PedidoRequestDTO("pv-1",
                List.of(new PedidoLineaRequestDTO("prod-1", 1), new PedidoLineaRequestDTO("prod-1", 2))), CORREO));
        verify(repositorioPedido, never()).save(any(Pedido.class));
    }

    @Test
    void elBorradorRechazaUnProductoSinPrecioVigente() throws MiException {
        when(servicioVigenciaPrecio.obtenerVigenciaVigente("prod-1")).thenReturn(Optional.empty());

        assertThrows(MiException.class, () -> servicio.crearBorrador(
                new PedidoRequestDTO("pv-1", List.of(new PedidoLineaRequestDTO("prod-1", 1))), CORREO));
        verify(repositorioPedido, never()).save(any(Pedido.class));
    }

    @Test
    void elBorradorRechazaCantidadesNoPositivasYListasVacias() {
        assertThrows(MiException.class, () -> servicio.crearBorrador(new PedidoRequestDTO("pv-1", List.of()), CORREO));
        assertThrows(MiException.class, () -> servicio.crearBorrador(new PedidoRequestDTO(null,
                List.of(new PedidoLineaRequestDTO("prod-1", 1))), CORREO));
        assertThrows(MiException.class, () -> servicio.crearBorrador(new PedidoRequestDTO("pv-1",
                List.of(new PedidoLineaRequestDTO("prod-1", 0))), CORREO));
    }

    @Test
    void confirmarReservaTodasLasPresentacionesConvertidasAUnidades() throws MiException {
        Pedido pedido = pedidoEnBorrador(2);
        when(repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse("ped-1", "prev-1"))
                .thenReturn(Optional.of(pedido));
        when(servicioStock.cantidadDisponibleParaPedido("prod-1", "ped-1")).thenReturn(48);

        PedidoResponseDTO confirmado = servicio.confirmar("ped-1", CORREO);

        assertEquals(EstadoPedido.CONFIRMADO, confirmado.estado());
        assertNotNull(confirmado.fechaConfirmacion());
        verify(servicioStock).bloquearParaReserva("prod-1");
        verify(repositorioReservaStock).save(any(ReservaStock.class));
    }

    @Test
    void confirmarRechazaLaSobreventaSinGuardarReservas() throws MiException {
        Pedido pedido = pedidoEnBorrador(2);
        when(repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse("ped-1", "prev-1"))
                .thenReturn(Optional.of(pedido));
        when(servicioStock.cantidadDisponibleParaPedido("prod-1", "ped-1")).thenReturn(10);

        MiException error = assertThrows(MiException.class, () -> servicio.confirmar("ped-1", CORREO));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        assertEquals(EstadoPedido.BORRADOR, pedido.getEstado());
        verify(repositorioReservaStock, never()).save(any(ReservaStock.class));
    }

    @Test
    void confirmarRechazaUnPedidoSinLineas() throws MiException {
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setPuntoVenta(puntoVenta);
        pedido.setPreventista(preventista);
        when(repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse("ped-1", "prev-1"))
                .thenReturn(Optional.of(pedido));

        assertThrows(MiException.class, () -> servicio.confirmar("ped-1", CORREO));
        verify(repositorioReservaStock, never()).save(any(ReservaStock.class));
    }

    @Test
    void completarConsumeTodasLasReservasActivas() throws MiException {
        Pedido pedido = pedidoEnBorrador(2);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        ReservaStock reserva = reservaDe(pedido, 48);
        when(repositorioPedido.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(pedido));
        when(repositorioReservaStock.findByPedido_IdAndEliminadoFalse("ped-1")).thenReturn(List.of(reserva));

        PedidoResponseDTO completado = servicio.completarAdministrativo("ped-1");

        assertEquals(EstadoPedido.COMPLETADO, completado.estado());
        assertNotNull(completado.fechaCompletado());
        verify(servicioStock).consumirReserva("ped-1", "prod-1", 48);
    }

    @Test
    void completarExigeTodasLasReservasActivas() throws MiException {
        Pedido pedido = pedidoEnBorrador(2);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        when(repositorioPedido.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(pedido));
        when(repositorioReservaStock.findByPedido_IdAndEliminadoFalse("ped-1")).thenReturn(List.of());

        assertThrows(MiException.class, () -> servicio.completarAdministrativo("ped-1"));
        verify(servicioStock, never()).consumirReserva(anyString(), anyString(), any());
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    }

    @Test
    void completarRechazaUnPedidoQueNoEstaConfirmado() throws MiException {
        Pedido pedido = pedidoEnBorrador(1);
        when(repositorioPedido.findByIdAndEliminadoFalse("ped-1")).thenReturn(Optional.of(pedido));

        assertThrows(MiException.class, () -> servicio.completarAdministrativo("ped-1"));
    }

    @Test
    void cancelarLiberaTodasLasReservasActivas() throws MiException {
        Pedido pedido = pedidoEnBorrador(2);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        ReservaStock reserva = reservaDe(pedido, 48);
        when(repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse("ped-1", "prev-1"))
                .thenReturn(Optional.of(pedido));
        when(repositorioReservaStock.findByPedido_IdAndEliminadoFalse("ped-1")).thenReturn(List.of(reserva));

        PedidoResponseDTO cancelado = servicio.cancelar("ped-1", CORREO);

        assertEquals(EstadoPedido.CANCELADO, cancelado.estado());
        assertNotNull(reserva.getFechaLiberacion());
        assertNotNull(cancelado.fechaCancelacion());
    }

    @Test
    void cancelarRechazaUnPedidoYaCompletado() throws MiException {
        Pedido pedido = pedidoEnBorrador(1);
        pedido.setEstado(EstadoPedido.COMPLETADO);
        when(repositorioPedido.findByIdAndPreventista_IdAndEliminadoFalse("ped-1", "prev-1"))
                .thenReturn(Optional.of(pedido));

        assertThrows(MiException.class, () -> servicio.cancelar("ped-1", CORREO));
        assertEquals(EstadoPedido.COMPLETADO, pedido.getEstado());
    }

    @Test
    void unaCuentaQueNoEsDePreventistaNiJefeNoOperaPedidos() throws MiException {
        Usuario administrativo = new Usuario();
        administrativo.setId("usr-2");
        administrativo.setRol(RolUsuario.ADMINISTRATIVO);
        when(servicioUsuario.buscarActivoPorNombreUsuario(CORREO)).thenReturn(Optional.of(administrativo));

        assertThrows(MiException.class, () -> servicio.crearBorrador(
                new PedidoRequestDTO("pv-1", List.of(new PedidoLineaRequestDTO("prod-1", 1))), CORREO));
        verify(repositorioPedido, never()).save(any(Pedido.class));
    }

    private Pedido pedidoEnBorrador(int cantidadPresentaciones) {
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setPuntoVenta(puntoVenta);
        pedido.setPreventista(preventista);
        pedido.setLineas(new ArrayList<>());
        LineaPedido linea = new LineaPedido();
        linea.setPedido(pedido);
        linea.setProducto(alfajor);
        linea.setNombreProducto(alfajor.getNombre());
        linea.setPrecioUnitario(new BigDecimal("100.00"));
        linea.setCantidadPresentaciones(cantidadPresentaciones);
        linea.setSubtotal(new BigDecimal("100.00").multiply(BigDecimal.valueOf(cantidadPresentaciones)));
        pedido.getLineas().add(linea);
        pedido.setTotal(linea.getSubtotal());
        return pedido;
    }

    private ReservaStock reservaDe(Pedido pedido, int cantidad) {
        ReservaStock reserva = new ReservaStock();
        reserva.setPedido(pedido);
        reserva.setProducto(alfajor);
        reserva.setCantidad(cantidad);
        reserva.setFechaCreacion(LocalDateTime.now());
        return reserva;
    }
}
