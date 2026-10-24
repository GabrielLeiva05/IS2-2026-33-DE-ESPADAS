package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.EstadoOrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleCompra;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompra;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class ServicioOrdenCompraTest {

    private RepositorioOrdenCompra repositorio;
    private ServicioOrdenCompra servicio;
    private ServicioStock servicioStock;
    private ServicioSucursal servicioSucursal;
    private ServicioVigenciaPrecio servicioVigenciaPrecio;
    private UsuarioServicio usuarios;
    private ServicioProducto servicioProducto;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioOrdenCompra.class);
        servicioStock = mock(ServicioStock.class);
        servicioSucursal = mock(ServicioSucursal.class);
        servicioVigenciaPrecio = mock(ServicioVigenciaPrecio.class);
        usuarios = mock(UsuarioServicio.class);
        servicioProducto = mock(ServicioProducto.class);
        servicio = new ServicioOrdenCompra();
        ReflectionTestUtils.setField(servicio, "repositorioOrdenCompra", repositorio);
        ReflectionTestUtils.setField(servicio, "servicioStock", servicioStock);
        ReflectionTestUtils.setField(servicio, "servicioSucursal", servicioSucursal);
        ReflectionTestUtils.setField(servicio, "servicioVigenciaPrecio", servicioVigenciaPrecio);
        ReflectionTestUtils.setField(servicio, "usuarioServicio", usuarios);
        ReflectionTestUtils.setField(servicio, "servicioProducto", servicioProducto);
        Sucursal sucursal = new Sucursal();
        sucursal.setId("sucursal-1");
        when(servicioSucursal.obtenerPrincipal()).thenReturn(sucursal);
    }

    @Test
    void clienteNoPuedeConsultarOrdenAjena() {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertThrows(AccessDeniedException.class,
                () -> servicio.buscarAccesibleParaUsuario("orden-1", "cliente-b@example.com", false));
    }

    @Test
    void clientePuedeConsultarSuOrden() throws Exception {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertSame(orden, servicio.buscarAccesibleParaUsuario("orden-1", "cliente-a@example.com", false));
    }

    @Test
    void crearOrdenAsociaElUsuarioAutenticado() throws Exception {
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);

        OrdenCompra creada = servicio.crearOrdenCompra("cliente-a@example.com");

        assertSame(usuario, creada.getUsuario());
        assertEquals("sucursal-1", creada.getSucursal().getId());
        assertEquals(EstadoOrdenCompra.PENDIENTE_DE_PAGO, creada.getEstadoOrdenCompra());
        verify(repositorio, never()).save(any(OrdenCompra.class));
    }

    @Test
    void consultarCarritoVacioNoPersisteUnaOrden() throws Exception {
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        usuario.setId("usuario-1");
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);
        when(repositorio.findByUsuario_IdAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                "usuario-1", EstadoOrdenCompra.PENDIENTE_DE_PAGO)).thenReturn(List.of());

        OrdenCompra carrito = servicio.obtenerCarrito("cliente-a@example.com");

        assertEquals(null, carrito.getId());
        assertEquals(0, carrito.getTotal());
        verify(repositorio, never()).save(any(OrdenCompra.class));
    }

    @Test
    void agregarPrimerProductoPersisteLaOrdenConDetalle() throws Exception {
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        usuario.setId("usuario-1");
        Producto producto = new Producto();
        producto.setId("producto-1");
        VigenciaPrecio vigencia = new VigenciaPrecio();
        vigencia.setFechaDesde(java.time.LocalDate.now());
        vigencia.setPrecio(7500);
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);
        when(repositorio.findByUsuario_IdAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                "usuario-1", EstadoOrdenCompra.PENDIENTE_DE_PAGO)).thenReturn(List.of());
        when(servicioProducto.buscarPorId("producto-1")).thenReturn(producto);
        when(servicioVigenciaPrecio.buscarVigenciaPrecioVigente("producto-1")).thenReturn(vigencia);
        when(servicioStock.cantidadActual("producto-1", "sucursal-1")).thenReturn(5);
        when(repositorio.save(any(OrdenCompra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdenCompra carrito = servicio.agregarAlCarrito("producto-1", 2, "cliente-a@example.com");

        assertEquals(1, carrito.getDetalles().size());
        assertEquals(15000, carrito.getTotal());
        verify(repositorio).save(carrito);
    }

    @Test
    void listarComprasEliminaCarritosVaciosPendientesAnteriores() throws Exception {
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        usuario.setId("usuario-1");
        OrdenCompra vacia = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        OrdenCompra pagada = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PAGO_REALIZADO);
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);
        when(repositorio.findByUsuario_IdAndEliminadoFalse("usuario-1")).thenReturn(List.of(vacia, pagada));

        List<OrdenCompra> ordenes = servicio.listarActivasDeUsuario("cliente-a@example.com");

        assertEquals(List.of(pagada), ordenes);
        verify(repositorio).deleteAll(List.of(vacia));
    }

    @Test
    void quitarUltimoProductoEliminaElCarritoVacio() throws Exception {
        Usuario usuario = new Usuario("cliente-a@example.com", "hash", RolUsuario.CLIENTE);
        usuario.setId("usuario-1");
        OrdenCompra carrito = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        carrito.setSucursal(sucursal("sucursal-1"));
        Producto producto = new Producto();
        producto.setId("producto-1");
        DetalleCompra detalle = new DetalleCompra();
        detalle.setId("detalle-1");
        detalle.setCantidad(2);
        detalle.setSubtotal(15000);
        detalle.setProducto(producto);
        carrito.getDetalles().add(detalle);
        VigenciaPrecio vigencia = new VigenciaPrecio();
        vigencia.setFechaDesde(java.time.LocalDate.now());
        vigencia.setPrecio(7500);
        when(usuarios.buscarActivoPorNombreUsuario("cliente-a@example.com")).thenReturn(usuario);
        when(repositorio.findByUsuario_IdAndEstadoOrdenCompraAndEliminadoFalseOrderByFechaDesc(
                "usuario-1", EstadoOrdenCompra.PENDIENTE_DE_PAGO)).thenReturn(List.of(carrito));
        when(servicioVigenciaPrecio.buscarVigenciaPrecioVigente("producto-1")).thenReturn(vigencia);

        servicio.eliminarDelCarrito("detalle-1", "cliente-a@example.com");

        verify(repositorio).delete(carrito);
        verify(repositorio, never()).save(any(OrdenCompra.class));
    }

    @Test
    void ordenSoloAvanzaPorLaSecuenciaDefinida() {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        assertThrows(IllegalStateException.class,
                () -> servicio.cambiarEstado("orden-1", EstadoOrdenCompra.ENTREGADO));
        verify(repositorio, never()).save(any());

        servicio.cambiarEstado("orden-1", EstadoOrdenCompra.PAGO_REALIZADO);
        assertEquals(EstadoOrdenCompra.PAGO_REALIZADO, orden.getEstadoOrdenCompra());
        verify(servicioStock).registrarVenta(orden);
    }

    @Test
    void pagoAprobadoDescuentaStockUnaSolaVezAunqueSeRepitaLaNotificacion() throws Exception {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        orden.setId("orden-1");
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        servicio.registrarResultadoMercadoPago("orden-1", "pago-1", "approved");
        servicio.registrarResultadoMercadoPago("orden-1", "pago-1", "approved");

        assertEquals(EstadoOrdenCompra.PAGO_REALIZADO, orden.getEstadoOrdenCompra());
        verify(servicioStock, times(1)).registrarVenta(orden);
    }

    @Test
    void registrarPreferenciaRecalculaElTotalDesdeLosDetalles() throws Exception {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PENDIENTE_DE_PAGO);
        orden.setTotal(0);
        Producto producto = new Producto();
        producto.setId("producto-1");
        producto.setNombre("Campera");
        DetalleCompra detalle = new DetalleCompra();
        detalle.setCantidad(2);
        detalle.setSubtotal(15000);
        detalle.setProducto(producto);
        orden.getDetalles().add(detalle);

        VigenciaPrecio vigencia = new VigenciaPrecio();
        vigencia.setFechaDesde(java.time.LocalDate.now());
        vigencia.setPrecio(7500);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));
        when(repositorio.save(any(OrdenCompra.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicioVigenciaPrecio.buscarVigenciaPrecioVigente("producto-1")).thenReturn(vigencia);
        when(servicioStock.cantidadActual("producto-1", "sucursal-1")).thenReturn(5);

        OrdenCompra actualizada = servicio.registrarPreferenciaMercadoPago(
                "orden-1", "cliente-a@example.com", "preferencia-1", "https://mercadopago.test/checkout");

        assertEquals(15000, actualizada.getTotal());
        assertEquals("preference_created", actualizada.getMercadoPagoStatus());
    }

    @Test
    void eliminarOrdenAplicaBajaLogicaAOrdenYDetalles() {
        OrdenCompra orden = ordenDe("cliente-a@example.com", EstadoOrdenCompra.PAGO_REALIZADO);
        DetalleCompra detalle = new DetalleCompra();
        detalle.setId("detalle-1");
        orden.getDetalles().add(detalle);
        ServicioDetalleCompra servicioDetalle = mock(ServicioDetalleCompra.class);
        ReflectionTestUtils.setField(servicio, "servicioDetalleCompra", servicioDetalle);
        when(repositorio.findById("orden-1")).thenReturn(Optional.of(orden));

        servicio.eliminarOrdenCompra("orden-1");

        assertEquals(true, orden.isEliminado());
        verify(servicioDetalle).eliminarDetalleCompra("detalle-1");
        verify(repositorio).save(orden);
    }

    private OrdenCompra ordenDe(String email, EstadoOrdenCompra estado) {
        OrdenCompra orden = new OrdenCompra();
        orden.setId("orden-1");
        orden.setUsuario(new Usuario(email, "hash", RolUsuario.CLIENTE));
        orden.setEstadoOrdenCompra(estado);
        return orden;
    }

    private Sucursal sucursal(String id) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        return sucursal;
    }
}