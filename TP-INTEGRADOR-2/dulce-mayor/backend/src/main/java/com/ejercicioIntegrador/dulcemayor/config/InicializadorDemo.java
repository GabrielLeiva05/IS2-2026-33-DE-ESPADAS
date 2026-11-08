package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.dto.AsignarRutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ConfirmarPagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PagoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoLineaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PromocionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SucursalRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.MedioPago;
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
import com.ejercicioIntegrador.dulcemayor.service.ServicioNewsletter;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPago;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioRuta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSucursal;
import com.ejercicioIntegrador.dulcemayor.service.ServicioUsuario;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVisita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dataset de demostración para probar el flujo completo sin cargar datos a mano.
 *
 * <p>Se activa con {@code app.datos-demo.habilitado=true} ({@code DATOS_DEMO_HABILITADO=true} en el
 * {@code .env}); por defecto está apagado, así que no afecta pruebas ni producción. Es idempotente y
 * crea todo a través de los servicios reales, por lo que respeta las mismas validaciones que la API.</p>
 *
 * <p>Genera 12 comercios distribuidos en cuatro departamentos de Mendoza, catálogo con precios y
 * stock, pedidos en distintos estados (confirmado, pago acreditado, entregado y cancelado), una hoja
 * de ruta con paradas pendientes y el plan de visitas del día. Así los mapas del preventista y del
 * distribuidor muestran un recorrido realista.</p>
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
    public static final String CORREO_COMERCIO_DEMO = "kiosco.demo@dulcemayor.test";
    public static final String NOMBRE_COMERCIO_DEMO = "Kiosco Demo";
    public static final String CODIGO_PROMOCION = "DEMO10";

    /** PNG 1x1 transparente: alcanza para cumplir la regla de "al menos una imagen por producto". */
    private static final byte[] PNG_DEMO = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFAAH/q842iQAAAABJRU5ErkJggg==");

    /** Producto de ejemplo. */
    private record ProductoDemo(String codigo, String nombre, Presentacion presentacion, int unidades,
                                double precio, int stock, boolean enOferta, String subCategoria) {
    }

    private static final List<ProductoDemo> PRODUCTOS = List.of(
            new ProductoDemo("DEMO-001", "Alfajor de dulce de leche", Presentacion.CAJA, 24, 18000.00, 2400, true, "Chocolates"),
            new ProductoDemo("DEMO-002", "Alfajor triple", Presentacion.CAJA, 24, 21600.00, 2400, false, "Chocolates"),
            new ProductoDemo("DEMO-003", "Bombones surtidos", Presentacion.PACK, 12, 15500.00, 2400, false, "Chocolates"),
            new ProductoDemo("DEMO-004", "Caramelos de miel", Presentacion.BULTO, 50, 9000.00, 3000, false, "Caramelos"),
            new ProductoDemo("DEMO-005", "Galletitas de chocolate", Presentacion.DISPLAY, 20, 12400.00, 2400, true, "Galletitas"),
            new ProductoDemo("DEMO-006", "Turrón de maní", Presentacion.UNIDAD, 1, 700.00, 3000, false, "Caramelos"),
            new ProductoDemo("DEMO-007", "Chocolate en rama", Presentacion.CAJA, 20, 24000.00, 2400, false, "Chocolates"),
            new ProductoDemo("DEMO-008", "Obleas con dulce de leche", Presentacion.PACK, 10, 8600.00, 2400, false, "Galletitas"),
            new ProductoDemo("DEMO-009", "Chupetines surtidos", Presentacion.BULTO, 100, 11000.00, 3000, false, "Caramelos"),
            new ProductoDemo("DEMO-010", "Mantecol", Presentacion.UNIDAD, 1, 950.00, 3000, true, "Chocolates"),
            new ProductoDemo("DEMO-011", "Alfajor de maicena", Presentacion.CAJA, 12, 13200.00, 2400, false, "Galletitas"),
            new ProductoDemo("DEMO-012", "Gomitas de fruta", Presentacion.PACK, 24, 14200.00, 2400, false, "Caramelos"));

    /** Comercio de ejemplo con su ubicación real aproximada dentro del Gran Mendoza. */
    private record ComercioDemo(String nombre, String responsable, String apellido, String documento,
                                String localidad, String calle, String numeracion, double latitud, double longitud) {
    }

    private static final List<ComercioDemo> COMERCIOS = List.of(
            new ComercioDemo("Kiosco Demo", "Marta", "Gómez", "31000001", "Ciudad de Mendoza",
                    "Las Heras", "250", -32.8912, -68.8401),
            new ComercioDemo("Almacén Don Pedro", "Pedro", "Ramírez", "31000002", "Ciudad de Mendoza",
                    "San Martín", "780", -32.8895, -68.8458),
            new ComercioDemo("Kiosco Sol", "Lucía", "Fernández", "31000003", "Ciudad de Mendoza",
                    "Sarmiento", "410", -32.8950, -68.8380),
            new ComercioDemo("Drugstore Central", "Ana", "López", "31000004", "Ciudad de Mendoza",
                    "Colón", "120", -32.8870, -68.8430),
            new ComercioDemo("Kiosco Estación", "Jorge", "Silva", "31000005", "Godoy Cruz",
                    "Rivadavia", "1550", -32.9270, -68.8330),
            new ComercioDemo("Autoservicio Norte", "Silvia", "Rojas", "31000006", "Las Heras",
                    "San Miguel", "330", -32.8530, -68.8290),
            new ComercioDemo("Kiosco Plaza", "Martín", "Aguirre", "31000007", "Godoy Cruz",
                    "Perito Moreno", "90", -32.9300, -68.8480),
            new ComercioDemo("Almacén Sur", "Carla", "Medina", "31000008", "Luján de Cuyo",
                    "Ruta 15", "2200", -33.0350, -68.8780),
            new ComercioDemo("Kiosco Guaymallén", "Diego", "Pereyra", "31000009", "Villa Nueva",
                    "Bandera de los Andes", "900", -32.8930, -68.7870),
            new ComercioDemo("Mercadito Maipú", "Verónica", "Castro", "31000010", "Maipú",
                    "Pueyrredón", "640", -32.9800, -68.7900),
            new ComercioDemo("Kiosco Terminal", "Gustavo", "Herrera", "31000011", "Ciudad de Mendoza",
                    "Videla Castillo", "1100", -32.8860, -68.8380),
            new ComercioDemo("Almacén Los Andes", "Paula", "Benítez", "31000012", "Las Heras",
                    "Chile", "2450", -32.8470, -68.8390));

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
    private final ServicioPago servicioPago;
    private final ServicioRuta servicioRuta;
    private final ServicioVisita servicioVisita;
    private final ServicioNewsletter servicioNewsletter;

    public InicializadorDemo(ServicioEmpleado servicioEmpleado, ServicioUsuario servicioUsuario,
                             ServicioLocalidad servicioLocalidad, ServicioCategoria servicioCategoria,
                             ServicioSubCategoria servicioSubCategoria, ServicioProducto servicioProducto,
                             ServicioVigenciaPrecio servicioVigenciaPrecio, ServicioStock servicioStock,
                             RepositorioStock repositorioStock, ServicioImagen servicioImagen,
                             ServicioPromocion servicioPromocion, ServicioSucursal servicioSucursal,
                             ServicioPuntoVenta servicioPuntoVenta, ServicioPedido servicioPedido,
                             ServicioPago servicioPago, ServicioRuta servicioRuta, ServicioVisita servicioVisita,
                             ServicioNewsletter servicioNewsletter) {
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
        this.servicioPago = servicioPago;
        this.servicioRuta = servicioRuta;
        this.servicioVisita = servicioVisita;
        this.servicioNewsletter = servicioNewsletter;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            crearEmpleados();
            crearCatalogo();
            crearPromocion();
            crearSucursales();
            List<PuntoVentaResponseDTO> comercios = crearComercios();
            crearPedidosDeEjemplo(comercios);
            crearHojaDeRuta();
            servicioVisita.generar(CORREO_PREVENTISTA, LocalDate.now());
            crearSuscriptoresNewsletter();
            LOG.info("""

                    ==== Datos de demostración listos ====
                    Empleados  : {} / {} / {}  (clave temporal '{}', se cambia al ingresar)
                    Comercios  : {} en cuatro departamentos de Mendoza
                    Cliente    : {} (registrate en el sitio con este correo para ver sus pedidos)
                    Promoción  : {}
                    """, CORREO_ADMINISTRATIVO, CORREO_PREVENTISTA, CORREO_DISTRIBUIDOR,
                    ServicioUsuario.CLAVE_TEMPORAL, comercios.size(), CORREO_COMERCIO_DEMO, CODIGO_PROMOCION);
        } catch (MiException e) {
            LOG.error("No se pudieron crear los datos de demostración: {}", e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ empleados

    private void crearEmpleados() throws MiException {
        crearEmpleado("Ana", "Administración", "30000001", CORREO_ADMINISTRATIVO, RolUsuario.ADMINISTRATIVO,
                "EMP-DEMO-01", "Ciudad de Mendoza", null, null, null);
        crearEmpleado("Pablo", "Preventista", "30000002", CORREO_PREVENTISTA, RolUsuario.PREVENTISTA,
                "EMP-DEMO-02", "Ciudad de Mendoza", "Mendoza Capital", null, null);
        crearEmpleado("Diego", "Distribuidor", "30000003", CORREO_DISTRIBUIDOR, RolUsuario.DISTRIBUIDOR,
                "EMP-DEMO-03", "Godoy Cruz", null, "Fiat Fiorino", "AB123CD");
    }

    private void crearEmpleado(String nombre, String apellido, String documento, String correo, RolUsuario rol,
                               String legajo, String localidad, String zona, String vehiculo, String patente)
            throws MiException {
        if (servicioUsuario.buscarActivoPorNombreUsuario(correo).isPresent()) {
            LOG.info("El empleado {} ya existe: no se recrea", correo);
            return;
        }
        DireccionRequestDTO direccion = new DireccionRequestDTO("San Martín", "100", "Centro", null, null,
                "Dato de demostración", -32.8895, -68.8458, idLocalidad(localidad));
        servicioEmpleado.alta(new EmpleadoRequestDTO(nombre, apellido, LocalDate.of(1990, 1, 1),
                TipoDocumento.DNI, documento, correo, rol, legajo, LocalDate.now().minusMonths(3), direccion,
                zona, vehiculo, patente, null));
        LOG.info("Empleado de demostración creado: {} ({})", correo, rol);
    }

    // ------------------------------------------------------------------ catálogo

    private void crearCatalogo() throws MiException {
        String idChocolates = crearSubcategoria("Chocolates", crearCategoria("Golosinas"));
        String idCaramelos = crearSubcategoria("Caramelos", crearCategoria("Golosinas"));
        String idGalletitas = crearSubcategoria("Galletitas", crearCategoria("Almacén"));
        for (ProductoDemo producto : PRODUCTOS) {
            String idSubCategoria = switch (producto.subCategoria()) {
                case "Chocolates" -> idChocolates;
                case "Caramelos" -> idCaramelos;
                default -> idGalletitas;
            };
            crearProductoConPrecioYStock(producto, idSubCategoria);
        }
        LOG.info("Catálogo de demostración: {} productos con precio vigente y stock", PRODUCTOS.size());
    }

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
        String idProducto = null;
        for (ProductoResponseDTO existente : servicioProducto.listar()) {
            if (existente.codigo().equalsIgnoreCase(producto.codigo())) {
                idProducto = existente.id();
            }
        }
        if (idProducto == null) {
            idProducto = servicioProducto.alta(new ProductoRequestDTO(producto.codigo(), producto.nombre(),
                    "Producto de demostración para probar el flujo completo", producto.presentacion(),
                    producto.unidades(), producto.enOferta(), idSubCategoria,
                    List.of(idImagenProducto(producto.codigo())))).id();
        }
        if (servicioVigenciaPrecio.obtenerVigenciaVigente(idProducto).isEmpty()) {
            servicioVigenciaPrecio.alta(new VigenciaPrecioRequestDTO(LocalDate.now().minusDays(1), null,
                    producto.precio(), idProducto));
        }
        if (repositorioStock.findByProducto_IdAndEliminadoFalse(idProducto).isEmpty()) {
            servicioStock.alta(new StockRequestDTO(idProducto, producto.stock(), 20));
        }
        return idProducto;
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

    // ------------------------------------------------------------------ comercial

    private void crearPromocion() throws MiException {
        boolean existe = servicioPromocion.listar().stream()
                .anyMatch(promocion -> promocion.codigo().equalsIgnoreCase(CODIGO_PROMOCION));
        if (existe) {
            return;
        }
        servicioPromocion.alta(new PromocionRequestDTO(CODIGO_PROMOCION, "10% de descuento de demostración",
                TipoPromocion.PORCENTAJE, BigDecimal.TEN, LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(180), true, false, null));
        LOG.info("Promoción de demostración creada: {}", CODIGO_PROMOCION);
    }

    private void crearSucursales() throws MiException {
        crearSucursal("Casa Central", "Ciudad de Mendoza", "San Martín", "500", "Frente a la plaza",
                -32.8895, -68.8458);
        crearSucursal("Sucursal Godoy Cruz", "Godoy Cruz", "Rivadavia", "1200", "A dos cuadras de la terminal",
                -32.9270, -68.8330);
    }

    private void crearSucursal(String nombre, String localidad, String calle, String numeracion, String referencia,
                               double latitud, double longitud) throws MiException {
        boolean existe = servicioSucursal.listarTodas().stream()
                .anyMatch(sucursal -> sucursal.nombre().equalsIgnoreCase(nombre));
        if (existe) {
            return;
        }
        servicioSucursal.alta(new SucursalRequestDTO(nombre, "2615550000",
                "sucursal@dulcemayor.test", "Lunes a viernes de 9 a 18",
                new DireccionRequestDTO(calle, numeracion, "Centro", null, null, referencia, latitud, longitud,
                        idLocalidad(localidad)), true));
        LOG.info("Sucursal de demostración creada: {}", nombre);
    }

    // ------------------------------------------------------------------ comercios

    private List<PuntoVentaResponseDTO> crearComercios() throws MiException {
        List<PuntoVentaResponseDTO> existentes = new ArrayList<>(
                servicioPuntoVenta.listarCartera(CORREO_PREVENTISTA));
        List<PuntoVentaResponseDTO> comercios = new ArrayList<>();
        for (ComercioDemo demo : COMERCIOS) {
            PuntoVentaResponseDTO encontrado = null;
            for (PuntoVentaResponseDTO punto : existentes) {
                if (punto.nombreComercial().equalsIgnoreCase(demo.nombre())) {
                    encontrado = punto;
                }
            }
            if (encontrado != null) {
                comercios.add(encontrado);
                continue;
            }
            String correo = demo.nombre().equals(NOMBRE_COMERCIO_DEMO) ? CORREO_COMERCIO_DEMO
                    : "comercio" + demo.documento().substring(Math.max(0, demo.documento().length() - 2))
                    + "@dulcemayor.test";
            PuntoVentaResponseDTO creado = servicioPuntoVenta.registrar(new PuntoVentaRequestDTO(
                    demo.responsable(), demo.apellido(), correo, LocalDate.of(1985, 6, 12), TipoDocumento.DNI,
                    demo.documento(), demo.nombre(), "261555" + demo.documento().substring(6), correo,
                    new DireccionRequestDTO(demo.calle(), demo.numeracion(), "Centro", null, null,
                            "Comercio de demostración", demo.latitud(), demo.longitud(),
                            idLocalidad(demo.localidad()))), CORREO_PREVENTISTA);
            comercios.add(creado);
            existentes.add(creado);
        }
        LOG.info("Comercios de demostración disponibles: {}", comercios.size());
        return comercios;
    }

    // ------------------------------------------------------------------ pedidos

    /** Índices de pedidos que quedan confirmados esperando pago (van a la hoja de ruta). */
    private final List<String> pedidosParaRuta = new ArrayList<>();
    /** Pedidos que se entregan en la carga demo para mostrar el recorrido mixto. */
    private final List<String> pedidosParaEntregar = new ArrayList<>();

    /**
     * Pedidos de ejemplo por comercio: 0..3 confirmados a la espera de pago, 4..5 y 6..7 con
     * transferencia acreditada (completados), 8..9 se entregan por ruta, 10 se cancela y 11 queda
     * sin pedidos para probar el flujo manual.
     */
    private void crearPedidosDeEjemplo(List<PuntoVentaResponseDTO> comercios) throws MiException {
        if (!servicioPedido.listarMisPedidos(CORREO_PREVENTISTA).isEmpty()) {
            LOG.info("El preventista de demostración ya tiene pedidos: no se crean más");
            return;
        }
        for (int i = 0; i < comercios.size(); i++) {
            String idPedido = crearPedidoConfirmado(comercios.get(i), i);
            if (i == 10) {
                servicioPedido.cancelar(idPedido, CORREO_PREVENTISTA);
            } else if (i == 4 || i == 5 || i == 6 || i == 7) {
                acreditarTransferencia(idPedido);
            } else if (i == 8 || i == 9) {
                acreditarTransferencia(idPedido);
                pedidosParaEntregar.add(idPedido);
            } else if (i == 11) {
                pedidosParaRuta.add(idPedido);
            } else {
                pedidosParaRuta.add(idPedido);
            }
        }
        LOG.info("Pedidos de demostración creados: {} en cola de reparto, {} para entregar en la demo",
                pedidosParaRuta.size(), pedidosParaEntregar.size());
    }

    private String crearPedidoConfirmado(PuntoVentaResponseDTO comercio, int indice) throws MiException {
        int cantidadProductos = 1 + (indice % 3);
        List<PedidoLineaRequestDTO> lineas = new ArrayList<>();
        for (int j = 0; j < cantidadProductos; j++) {
            ProductoDemo producto = PRODUCTOS.get((indice + j) % PRODUCTOS.size());
            lineas.add(new PedidoLineaRequestDTO(idProductoPorCodigo(producto.codigo()), 1 + ((indice + j) % 3)));
        }
        PedidoResponseDTO borrador = servicioPedido.crearBorrador(
                new PedidoRequestDTO(comercio.id(), lineas), CORREO_PREVENTISTA);
        if (indice % 4 == 0) {
            servicioPedido.aplicarPromocion(borrador.id(), CODIGO_PROMOCION, CORREO_PREVENTISTA);
        }
        return servicioPedido.confirmar(borrador.id(), CORREO_PREVENTISTA).id();
    }

    private void acreditarTransferencia(String idPedido) throws MiException {
        PagoResponseDTO pago = servicioPago.iniciarPorAdministrativo(idPedido,
                new PagoRequestDTO(MedioPago.TRANSFERENCIA));
        servicioPago.acreditar(pago.id(), new ConfirmarPagoRequestDTO("DEMO-" + pago.referenciaOperacion()
                .substring(0, 8).toUpperCase(java.util.Locale.ROOT), "Transferencia acreditada en la carga demo"));
    }
    // ------------------------------------------------------------------ ruta y visitas

    private void crearHojaDeRuta() throws MiException {
        String idDistribuidor = idEmpleadoDe(CORREO_DISTRIBUIDOR);
        boolean existe = servicioRuta.listar(LocalDate.now()).stream()
                .anyMatch(ruta -> idDistribuidor.equals(ruta.idDistribuidor()));
        if (existe) {
            LOG.info("La hoja de ruta de hoy ya existe: no se recrea");
            return;
        }
        List<String> ids = new ArrayList<>(pedidosParaRuta);
        ids.addAll(pedidosParaEntregar);
        if (ids.isEmpty()) {
            return;
        }
        RutaResponseDTO ruta = servicioRuta.asignar(new AsignarRutaRequestDTO(LocalDate.now(), idDistribuidor, ids,
                "Reparto de demostración"));
        // Las últimas paradas se entregan para que el mapa muestre un recorrido mixto (entregado + pendiente)
        List<com.ejercicioIntegrador.dulcemayor.dto.ParadaResponseDTO> paradas = ruta.paradas();
        int entregadas = 0;
        for (int i = paradas.size() - 1; i >= 0 && entregadas < pedidosParaEntregar.size(); i--, entregadas++) {
            servicioRuta.entregar(paradas.get(i).id(),
                    new com.ejercicioIntegrador.dulcemayor.dto.CerrarParadaRequestDTO(null,
                            "Entrega registrada en la carga demo"), CORREO_DISTRIBUIDOR);
        }
        LOG.info("Hoja de ruta de demostración con {} paradas ({} entregadas)", paradas.size(), entregadas);
    }
    private void crearSuscriptoresNewsletter() {
        for (String correo : List.of("newsletter1@dulcemayor.test", "newsletter2@dulcemayor.test")) {
            try {
                servicioNewsletter.suscribir(correo);
            } catch (MiException e) {
                LOG.debug("Suscriptor de demostración {} sin alta: {}", correo, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ auxiliares

    private String idProductoPorCodigo(String codigo) throws MiException {
        return servicioProducto.buscarProductoPorCodigo(codigo).id();
    }

    private String idEmpleadoDe(String correo) {
        String idUsuario = servicioUsuario.buscarActivoPorNombreUsuario(correo).orElseThrow().getId();
        return servicioEmpleado.buscarPorUsuario(idUsuario).orElseThrow().getId();
    }

    private String idLocalidad(String nombre) throws MiException {
        return servicioLocalidad.buscarLocalidadPorNombre(nombre).id();
    }
}
