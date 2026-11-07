package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoLineaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SucursalRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoPromocion;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.ServicioImagen;
import com.ejercicioIntegrador.dulcemayor.service.ServicioLocalidad;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSucursal;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dataset de demostración para probar el flujo completo sin cargar datos a mano.
 * Se activa con {@code app.datos-demo.habilitado=true} (por defecto está apagado, así que no afecta
 * pruebas ni producción). Es idempotente y crea todo a través de los servicios reales, por lo que
 * respeta las mismas validaciones que la API.
 *
 * <p>Los empleados se crean con la clave temporal {@link ServicioUsuario#CLAVE_TEMPORAL}
 * ("dulcemayor") y {@code debeCambiarClave = true}: el primer ingreso obliga a cambiarla, igual que
 * cualquier alta hecha desde el panel.</p>
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.datos-demo.habilitado", havingValue = "true")
public class InicializadorDemo implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorDemo.class);

    public static final String CORREO_ADMINISTRATIVO = "admin@dulcemayor.test";
    public static final String CORREO_PREVENTISTA = "preventista@dulcemayor.test";
    public static final String CORREO_DISTRIBUIDOR = "distribuidor@dulcemayor.test";
    public static final String CORREO_COMERCIO = "kiosco.demo@dulcemayor.test";
    public static final String NOMBRE_COMERCIO = "Kiosco Demo";
    public static final String CODIGO_PROMOCION = "DEMO10";
    public static final String NOMBRE_SUCURSAL = "Casa Central";

    private static final String LOCALIDAD = "Ciudad de Mendoza";

    /** PNG 1x1 transparente: alcanza para cumplir la regla de "al menos una imagen por producto". */
    private static final byte[] PNG_DEMO = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFAAH/q842iQAAAABJRU5ErkJggg==");

    /** Productos de ejemplo: código, nombre, presentación, unidades y precio de lista. */
    private record ProductoDemo(String codigo, String nombre, Presentacion presentacion, int unidades,
                                double precio, int stock, boolean enOferta) {
    }

    private static final List<ProductoDemo> PRODUCTOS = List.of(
            new ProductoDemo("DEMO-001", "Alfajor de dulce de leche", Presentacion.CAJA, 24, 18000.00, 60, true),
            new ProductoDemo("DEMO-002", "Alfajor triple", Presentacion.CAJA, 24, 21600.00, 45, false),
            new ProductoDemo("DEMO-003", "Bombones surtidos", Presentacion.PACK, 12, 15500.00, 40, false),
            new ProductoDemo("DEMO-004", "Caramelos de miel", Presentacion.BULTO, 50, 9000.00, 80, false),
            new ProductoDemo("DEMO-005", "Galletitas de chocolate", Presentacion.DISPLAY, 20, 12400.00, 55, true),
            new ProductoDemo("DEMO-006", "Turrón de maní", Presentacion.UNIDAD, 1, 700.00, 120, false));

    private final ServicioEmpleado servicioEmpleado;
    private final ServicioUsuario servicioUsuario;
    private final ServicioLocalidad servicioLocalidad;
    private final ServicioCategoria servicioCategoria;
    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioProducto servicioProducto;
    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioStock servicioStock;
    private final RepositorioStock repositorioStock;
    private final ServicioImagen servicioImagen;
    private final ServicioPromocion servicioPromocion;
    private final ServicioSucursal servicioSucursal;
    private final ServicioPuntoVenta servicioPuntoVenta;
    private final ServicioPedido servicioPedido;

    public InicializadorDemo(ServicioEmpleado servicioEmpleado, ServicioUsuario servicioUsuario,
                             ServicioLocalidad servicioLocalidad, ServicioCategoria servicioCategoria,
                             ServicioSubCategoria servicioSubCategoria, ServicioProducto servicioProducto,
                             ServicioVigenciaPrecio servicioVigenciaPrecio, ServicioStock servicioStock,
                             RepositorioStock repositorioStock, ServicioImagen servicioImagen,
                             ServicioPromocion servicioPromocion,
                             ServicioSucursal servicioSucursal, ServicioPuntoVenta servicioPuntoVenta,
                             ServicioPedido servicioPedido) {
        this.servicioEmpleado = servicioEmpleado;
        this.servicioUsuario = servicioUsuario;
        this.servicioLocalidad = servicioLocalidad;
        this.servicioCategoria = servicioCategoria;
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioStock = servicioStock;
        this.repositorioStock = repositorioStock;
        this.servicioImagen = servicioImagen;
        this.servicioPromocion = servicioPromocion;
        this.servicioSucursal = servicioSucursal;
        this.servicioPuntoVenta = servicioPuntoVenta;
        this.servicioPedido = servicioPedido;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String idLocalidad = servicioLocalidad.buscarLocalidadPorNombre(LOCALIDAD).id();
            crearEmpleados(idLocalidad);
            String idChocolates = crearSubcategoria("Chocolates", crearCategoria("Golosinas"));
            String idCaramelos = crearSubcategoria("Caramelos", crearCategoria("Golosinas"));
            String idGalletitas = crearSubcategoria("Galletitas", crearCategoria("Almacén"));
            for (ProductoDemo producto : PRODUCTOS) {
                String idSubCategoria = switch (producto.codigo()) {
                    case "DEMO-001", "DEMO-002", "DEMO-003" -> idChocolates;
                    case "DEMO-004" -> idCaramelos;
                    default -> idGalletitas;
                };
                crearProductoConPrecioYStock(producto, idSubCategoria);
            }
            crearPromocion();
            crearSucursal(idLocalidad);
            crearComercioYPedidoDemo();
            LOG.info("Datos de demostración listos. Empleados: {} / {} / {} (clave temporal '{}', se debe cambiar "
                            + "al ingresar). Comercio: {} con email {}.",
                    CORREO_ADMINISTRATIVO, CORREO_PREVENTISTA, CORREO_DISTRIBUIDOR,
                    ServicioUsuario.CLAVE_TEMPORAL, NOMBRE_COMERCIO, CORREO_COMERCIO);
        } catch (MiException e) {
            LOG.error("No se pudieron crear los datos de demostración: {}", e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ empleados

    private void crearEmpleados(String idLocalidad) throws MiException {
        crearEmpleado("Ana", "Administración", "30000001", CORREO_ADMINISTRATIVO, RolUsuario.ADMINISTRATIVO,
                "EMP-DEMO-01", idLocalidad, null, null, null);
        crearEmpleado("Pablo", "Preventista", "30000002", CORREO_PREVENTISTA, RolUsuario.PREVENTISTA,
                "EMP-DEMO-02", idLocalidad, "Mendoza Capital", null, null);
        crearEmpleado("Diego", "Distribuidor", "30000003", CORREO_DISTRIBUIDOR, RolUsuario.DISTRIBUIDOR,
                "EMP-DEMO-03", idLocalidad, null, "Fiat Fiorino", "AB123CD");
    }

    private void crearEmpleado(String nombre, String apellido, String documento, String correo, RolUsuario rol,
                               String legajo, String idLocalidad, String zona, String vehiculo, String patente)
            throws MiException {
        if (servicioUsuario.buscarActivoPorNombreUsuario(correo).isPresent()) {
            LOG.info("El empleado {} ya existe: no se recrea", correo);
            return;
        }
        DireccionRequestDTO direccion = new DireccionRequestDTO("San Martín", "100", "Centro", null, null,
                "Dato de demostración", -32.8895, -68.8458, idLocalidad);
        servicioEmpleado.alta(new EmpleadoRequestDTO(nombre, apellido, LocalDate.of(1990, 1, 1),
                TipoDocumento.DNI, documento, correo, rol, legajo, LocalDate.now().minusMonths(3), direccion,
                zona, vehiculo, patente, null));
        LOG.info("Empleado de demostración creado: {} ({})", correo, rol);
    }

    // ------------------------------------------------------------------ catálogo

    private String crearCategoria(String nombre) throws MiException {
        for (CategoriaResponseDTO existente : servicioCategoria.listar()) {
            if (existente.nombre().equalsIgnoreCase(nombre)) {
                return existente.id();
            }
        }
        return servicioCategoria.alta(new CategoriaRequestDTO(nombre)).id();
    }

    private String crearSubcategoria(String nombre, String idCategoria) throws MiException {
        for (SubCategoriaResponseDTO existente : servicioSubCategoria.listar()) {
            if (existente.nombre().equalsIgnoreCase(nombre) && idCategoria.equals(existente.idCategoria())) {
                return existente.id();
            }
        }
        return servicioSubCategoria.alta(new SubCategoriaRequestDTO(nombre, idCategoria)).id();
    }

    private String crearProductoConPrecioYStock(ProductoDemo producto, String idSubCategoria) throws MiException {
        String idProducto = servicioProducto.listar().stream()
                .filter(existente -> existente.codigo().equalsIgnoreCase(producto.codigo()))
                .findFirst()
                .map(existente -> existente.id())
                .orElse(null);
        if (idProducto == null) {
            idProducto = servicioProducto.alta(new ProductoRequestDTO(producto.codigo(), producto.nombre(),
                    "Producto de demostración para probar el flujo completo", producto.presentacion(),
                    producto.unidades(), producto.enOferta(), idSubCategoria,
                    List.of(idImagenProducto(producto.codigo())))).id();
            LOG.info("Producto de demostración creado: {}", producto.codigo());
        }
        if (servicioVigenciaPrecio.obtenerVigenciaVigente(idProducto).isEmpty()) {
            servicioVigenciaPrecio.alta(new VigenciaPrecioRequestDTO(LocalDate.now().minusDays(1), null,
                    producto.precio(), idProducto));
        }
        if (repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).isEmpty()) {
            servicioStock.alta(new StockRequestDTO(idProducto, producto.stock(), 5));
        }
        return idProducto;
    }

    // ------------------------------------------------------------------ comercial

    private void crearPromocion() throws MiException {
        boolean existe = servicioPromocion.listar().stream()
                .anyMatch(promocion -> promocion.codigo().equalsIgnoreCase(CODIGO_PROMOCION));
        if (existe) {
            LOG.info("La promoción {} ya existe: no se recrea", CODIGO_PROMOCION);
            return;
        }
        servicioPromocion.alta(new PromocionRequestDTO(CODIGO_PROMOCION, "10% de descuento de demostración",
                TipoPromocion.PORCENTAJE, BigDecimal.TEN, LocalDate.now().minusDays(30), LocalDate.now().plusDays(90),
                true, false, null));
        LOG.info("Promoción de demostración creada: {}", CODIGO_PROMOCION);
    }

    private void crearSucursal(String idLocalidad) throws MiException {
        boolean existe = servicioSucursal.listarTodas().stream()
                .anyMatch(sucursal -> sucursal.nombre().equalsIgnoreCase(NOMBRE_SUCURSAL));
        if (existe) {
            LOG.info("La sucursal {} ya existe: no se recrea", NOMBRE_SUCURSAL);
            return;
        }
        DireccionRequestDTO direccion = new DireccionRequestDTO("San Martín", "500", "Centro", null, null,
                "Frente a la plaza", -32.8895, -68.8458, idLocalidad);
        servicioSucursal.alta(new SucursalRequestDTO(NOMBRE_SUCURSAL, "2615550000",
                "casa-central@dulcemayor.test", "Lunes a viernes de 9 a 18", direccion, true));
        LOG.info("Sucursal de demostración creada: {}", NOMBRE_SUCURSAL);
    }

    // ------------------------------------------------------------------ comercio y pedido

    private void crearComercioYPedidoDemo() throws MiException {
        String idPuntoVenta = null;
        for (PuntoVentaResponseDTO existente : servicioPuntoVenta.listarCartera(CORREO_PREVENTISTA)) {
            if (existente.nombreComercial().equalsIgnoreCase(NOMBRE_COMERCIO)) {
                idPuntoVenta = existente.id();
            }
        }
        if (idPuntoVenta == null) {
            idPuntoVenta = servicioPuntoVenta.registrar(new PuntoVentaRequestDTO("Marta", "Gómez", CORREO_COMERCIO,
                    LocalDate.of(1985, 6, 12), TipoDocumento.DNI, "31000001", NOMBRE_COMERCIO, "2615551234",
                    CORREO_COMERCIO, new DireccionRequestDTO("Las Heras", "250", "Centro", null, null,
                    "Kiosco de la esquina", -32.8912, -68.8401,
                    servicioLocalidad.buscarLocalidadPorNombre(LOCALIDAD).id())), CORREO_PREVENTISTA).id();
            LOG.info("Comercio de demostración creado: {}", NOMBRE_COMERCIO);
        }
        if (!servicioPedido.listarMisPedidos(CORREO_PREVENTISTA).isEmpty()) {
            LOG.info("El preventista de demostración ya tiene pedidos: no se crea otro");
            return;
        }
        String idPedido = servicioPedido.crearBorrador(new PedidoRequestDTO(idPuntoVenta, List.of(
                new PedidoLineaRequestDTO(idProductoPorCodigo("DEMO-001"), 2),
                new PedidoLineaRequestDTO(idProductoPorCodigo("DEMO-004"), 1))), CORREO_PREVENTISTA).id();
        servicioPedido.confirmar(idPedido, CORREO_PREVENTISTA);
        LOG.info("Pedido de demostración confirmado (stock reservado): {}", idPedido);
    }

    private String idProductoPorCodigo(String codigo) throws MiException {
        return servicioProducto.buscarProductoPorCodigo(codigo).id();
    }

    /** Cada producto necesita su propia imagen (Producto 1 --> 1..* Imagen). */
    private String idImagenProducto(String codigo) throws MiException {
        String nombre = "demo-" + codigo.toLowerCase(java.util.Locale.ROOT) + ".png";
        for (ImagenResponseDTO existente : servicioImagen.listar()) {
            if (existente.nombre().equalsIgnoreCase(nombre)) {
                return existente.id();
            }
        }
        return servicioImagen.alta(new ImagenRequestDTO(nombre, "image/png", PNG_DEMO, TipoImagen.PRODUCTO)).id();
    }
}
