package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.config.InicializadorDemo;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSucursal;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el dataset de demostración contra una base H2 propia, para no interferir con el resto
 * de las pruebas de integración.
 */
@SpringBootTest(properties = {
        "app.datos-demo.habilitado=true",
        "spring.datasource.url=jdbc:h2:mem:dulcemayor-demo;DB_CLOSE_DELAY=-1;MODE=MySQL"})
class InicializadorDemoIntegrationTest {

    @Autowired
    private ServicioUsuario servicioUsuario;
    @Autowired
    private ServicioEmpleado servicioEmpleado;
    @Autowired
    private ServicioProducto servicioProducto;
    @Autowired
    private ServicioVigenciaPrecio servicioVigenciaPrecio;
    @Autowired
    private ServicioStock servicioStock;
    @Autowired
    private ServicioPromocion servicioPromocion;
    @Autowired
    private ServicioSucursal servicioSucursal;
    @Autowired
    private ServicioPuntoVenta servicioPuntoVenta;
    @Autowired
    private ServicioPedido servicioPedido;
    @Autowired
    private InicializadorDemo inicializadorDemo;

    @Test
    void creaEmpleadosCatalogoComercioYPedidoDeEjemplo() throws MiException {
        var administrativo = servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_ADMINISTRATIVO)
                .orElseThrow();
        assertEquals(RolUsuario.ADMINISTRATIVO, administrativo.getRol());
        assertTrue(administrativo.isDebeCambiarClave(), "el empleado nuevo arranca con clave temporal");

        assertTrue(servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_PREVENTISTA).isPresent());
        assertTrue(servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_DISTRIBUIDOR).isPresent());
        assertEquals(1, servicioEmpleado.listarPorRol(RolUsuario.PREVENTISTA).stream()
                .filter(empleado -> InicializadorDemo.CORREO_PREVENTISTA.equals(empleado.correo())).count());

        var producto = servicioProducto.buscarProductoPorCodigo("DEMO-001");
        assertEquals("Alfajor de dulce de leche", producto.nombre());
        assertEquals(18000.00, servicioVigenciaPrecio.buscarVigenciaPrecioVigente(producto.id()).precio());
        assertEquals(60, servicioStock.buscarStockPorProducto(producto.id()).cantidadActual());

        assertTrue(servicioPromocion.listar().stream()
                .anyMatch(promocion -> InicializadorDemo.CODIGO_PROMOCION.equals(promocion.codigo())
                        && promocion.vigente()));
        assertTrue(servicioSucursal.listarActivas().stream()
                .anyMatch(sucursal -> InicializadorDemo.NOMBRE_SUCURSAL.equals(sucursal.nombre())));

        var cartera = servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA);
        assertTrue(cartera.stream().anyMatch(punto -> InicializadorDemo.NOMBRE_COMERCIO.equals(punto.nombreComercial())));

        var pedidos = servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA);
        assertEquals(1, pedidos.size());
        assertEquals(EstadoPedido.CONFIRMADO, pedidos.get(0).estado());
        assertTrue(pedidos.get(0).total().signum() > 0);
    }

    @Test
    void esIdempotenteAlVolverAEjecutarse() throws MiException {
        int productos = servicioProducto.listar().size();
        int promociones = servicioPromocion.listar().size();
        int sucursales = servicioSucursal.listarTodas().size();
        int cartera = servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA).size();
        int pedidos = servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA).size();

        inicializadorDemo.run(null);

        assertEquals(productos, servicioProducto.listar().size());
        assertEquals(promociones, servicioPromocion.listar().size());
        assertEquals(sucursales, servicioSucursal.listarTodas().size());
        assertEquals(cartera, servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA).size());
        assertEquals(pedidos, servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA).size());
        assertFalse(servicioProducto.buscarProductoPorCodigo("DEMO-002").codigo().isBlank());
    }
}
