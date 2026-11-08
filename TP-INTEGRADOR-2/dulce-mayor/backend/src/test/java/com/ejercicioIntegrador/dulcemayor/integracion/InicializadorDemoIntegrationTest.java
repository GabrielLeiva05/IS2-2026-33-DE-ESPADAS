package com.ejercicioIntegrador.dulcemayor.integracion;

import com.ejercicioIntegrador.dulcemayor.config.InicializadorDemo;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.ServicioNewsletter;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioRuta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSucursal;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVisita;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica el dataset de demostración contra una base H2 propia, para no interferir con el resto de
 * las pruebas de integración.
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
    private ServicioRuta servicioRuta;
    @Autowired
    private ServicioVisita servicioVisita;
    @Autowired
    private ServicioNewsletter servicioNewsletter;
    @Autowired
    private InicializadorDemo inicializadorDemo;

    @Test
    void creaEmpleadosCatalogoYAlMenosDoceComercios() throws MiException {
        var administrativo = servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_ADMINISTRATIVO)
                .orElseThrow();
        assertEquals(RolUsuario.ADMINISTRATIVO, administrativo.getRol());
        assertTrue(administrativo.isDebeCambiarClave(), "el empleado nuevo arranca con clave temporal");
        assertTrue(servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_PREVENTISTA).isPresent());
        assertTrue(servicioUsuario.buscarActivoPorNombreUsuario(InicializadorDemo.CORREO_DISTRIBUIDOR).isPresent());
        assertEquals(1, servicioEmpleado.listarPorRol(RolUsuario.PREVENTISTA).stream()
                .filter(empleado -> InicializadorDemo.CORREO_PREVENTISTA.equals(empleado.correo())).count());

        var producto = servicioProducto.buscarProductoPorCodigo("DEMO-001");
        assertTrue(servicioVigenciaPrecio.buscarVigenciaPrecioVigente(producto.id()).precio() > 0);
        assertTrue(servicioStock.buscarStockPorProducto(producto.id()).cantidadActual() > 0);
        assertEquals(12, servicioProducto.listar().size());

        var cartera = servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA);
        assertTrue(cartera.size() >= 12, "se esperan al menos 12 comercios de prueba");
        assertTrue(cartera.stream().allMatch(punto -> punto.direccion().latitud() != null
                && punto.direccion().longitud() != null), "todos los comercios deben tener coordenadas");

        assertTrue(servicioPromocion.listar().stream()
                .anyMatch(promocion -> InicializadorDemo.CODIGO_PROMOCION.equals(promocion.codigo())));
        assertTrue(servicioSucursal.listarActivas().size() >= 2);
        assertTrue(servicioNewsletter.contarActivas() >= 2);
    }

    @Test
    void creaPedidosEnDistintosEstadosUnaRutaYElPlanDeVisitas() throws MiException {
        var pedidos = servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA);
        assertTrue(pedidos.size() >= 11, "se esperan pedidos de ejemplo en varios estados");
        assertTrue(pedidos.stream().anyMatch(pedido -> pedido.estado() == EstadoPedido.CONFIRMADO));
        assertTrue(pedidos.stream().anyMatch(pedido -> pedido.estado() == EstadoPedido.COMPLETADO));
        assertTrue(pedidos.stream().anyMatch(pedido -> pedido.estado() == EstadoPedido.CANCELADO));
        assertTrue(pedidos.stream().anyMatch(pedido -> pedido.estado() == EstadoPedido.ENTREGADO));

        var rutas = servicioRuta.listar(LocalDate.now());
        assertFalse(rutas.isEmpty(), "debe existir la hoja de ruta del día");
        var ruta = rutas.get(0);
        assertTrue(ruta.paradas().size() >= 4, "la hoja de ruta debe tener varias paradas para el mapa");
        assertTrue(ruta.paradas().stream().anyMatch(parada -> "PENDIENTE".equals(parada.estado().name())));
        assertTrue(ruta.paradas().stream().anyMatch(parada -> "ENTREGADO".equals(parada.estado().name())));

        var plan = servicioVisita.planDelDia(InicializadorDemo.CORREO_PREVENTISTA, LocalDate.now());
        assertTrue(plan.visitas().size() >= 12, "el plan de visitas debe incluir toda la cartera");
        assertEquals(1, plan.visitas().get(0).orden());
    }

    @Test
    void esIdempotenteAlVolverAEjecutarse() throws MiException {
        int productos = servicioProducto.listar().size();
        int promociones = servicioPromocion.listar().size();
        int sucursales = servicioSucursal.listarTodas().size();
        int cartera = servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA).size();
        int pedidos = servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA).size();
        int paradas = servicioRuta.listar(LocalDate.now()).get(0).paradas().size();
        int visitas = servicioVisita.planDelDia(InicializadorDemo.CORREO_PREVENTISTA, LocalDate.now())
                .visitas().size();

        inicializadorDemo.run(null);

        assertEquals(productos, servicioProducto.listar().size());
        assertEquals(promociones, servicioPromocion.listar().size());
        assertEquals(sucursales, servicioSucursal.listarTodas().size());
        assertEquals(cartera, servicioPuntoVenta.listarCartera(InicializadorDemo.CORREO_PREVENTISTA).size());
        assertEquals(pedidos, servicioPedido.listarMisPedidos(InicializadorDemo.CORREO_PREVENTISTA).size());
        assertEquals(paradas, servicioRuta.listar(LocalDate.now()).get(0).paradas().size());
        assertEquals(visitas, servicioVisita.planDelDia(InicializadorDemo.CORREO_PREVENTISTA, LocalDate.now())
                .visitas().size());
    }
}
