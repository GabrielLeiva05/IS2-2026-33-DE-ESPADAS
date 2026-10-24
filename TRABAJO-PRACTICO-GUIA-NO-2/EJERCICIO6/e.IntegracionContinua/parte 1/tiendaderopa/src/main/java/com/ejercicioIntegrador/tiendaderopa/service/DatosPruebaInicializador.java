package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoContacto;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoTelefono;
import com.ejercicioIntegrador.tiendaderopa.model.Categoria;
import com.ejercicioIntegrador.tiendaderopa.model.ContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.model.Departamento;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.model.DetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Direccion;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoFactura;
import com.ejercicioIntegrador.tiendaderopa.model.EstadoOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.FacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.FormaDePago;
import com.ejercicioIntegrador.tiendaderopa.model.Localidad;
import com.ejercicioIntegrador.tiendaderopa.model.OrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.model.ObjetivoReposicion;
import com.ejercicioIntegrador.tiendaderopa.model.Pais;
import com.ejercicioIntegrador.tiendaderopa.model.Persona;
import com.ejercicioIntegrador.tiendaderopa.model.Producto;
import com.ejercicioIntegrador.tiendaderopa.model.Proveedor;
import com.ejercicioIntegrador.tiendaderopa.model.Provincia;
import com.ejercicioIntegrador.tiendaderopa.model.Stock;
import com.ejercicioIntegrador.tiendaderopa.model.Sucursal;
import com.ejercicioIntegrador.tiendaderopa.model.SubCategoria;
import com.ejercicioIntegrador.tiendaderopa.model.TipoPago;
import com.ejercicioIntegrador.tiendaderopa.model.Usuario;
import com.ejercicioIntegrador.tiendaderopa.model.VigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.repository.DepartamentoRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.DireccionRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.LocalidadRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.ObjetivoReposicionRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.PaisRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.PersonaRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.ProvinciaRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioCategoria;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioContactoTelefonico;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleFactura;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioDetalleOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFacturaProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioFormaDePago;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioOrdenCompraProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProducto;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProveedor;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioStock;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioSubCategoria;
import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioVigenciaPrecio;
import com.ejercicioIntegrador.tiendaderopa.repository.SucursalRepositorio;
import com.ejercicioIntegrador.tiendaderopa.repository.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatosPruebaInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosPruebaInicializador.class);
    private static final List<String> SUBCATEGORIAS = List.of("Ropa", "Calzado", "Accesorios");

    private final PaisRepositorio paisRepositorio;
    private final ProvinciaRepositorio provinciaRepositorio;
    private final DepartamentoRepositorio departamentoRepositorio;
    private final LocalidadRepositorio localidadRepositorio;
    private final DireccionRepositorio direccionRepositorio;
    private final PersonaRepositorio personaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RepositorioContactoTelefonico contactoTelefonicoRepositorio;
    private final RepositorioCategoria categoriaRepositorio;
    private final RepositorioSubCategoria subCategoriaRepositorio;
    private final RepositorioProducto productoRepositorio;
    private final RepositorioVigenciaPrecio vigenciaPrecioRepositorio;
    private final RepositorioProveedor proveedorRepositorio;
    private final RepositorioFormaDePago formaDePagoRepositorio;
    private final RepositorioOrdenCompraProveedor ordenProveedorRepositorio;
    private final RepositorioDetalleOrdenCompraProveedor detalleOrdenProveedorRepositorio;
    private final RepositorioFacturaProveedor facturaProveedorRepositorio;
    private final RepositorioDetalleFactura detalleFacturaRepositorio;
    private final RepositorioStock stockRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final String claveDemo;

    @Autowired
    private SucursalRepositorio sucursalRepositorio;

    @Autowired
    private ObjetivoReposicionRepositorio objetivoReposicionRepositorio;

    public DatosPruebaInicializador(PaisRepositorio paisRepositorio,
            ProvinciaRepositorio provinciaRepositorio,
            DepartamentoRepositorio departamentoRepositorio,
            LocalidadRepositorio localidadRepositorio,
            DireccionRepositorio direccionRepositorio,
            PersonaRepositorio personaRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            RepositorioContactoTelefonico contactoTelefonicoRepositorio,
            RepositorioCategoria categoriaRepositorio,
            RepositorioSubCategoria subCategoriaRepositorio,
            RepositorioProducto productoRepositorio,
            RepositorioVigenciaPrecio vigenciaPrecioRepositorio,
            RepositorioProveedor proveedorRepositorio,
            RepositorioFormaDePago formaDePagoRepositorio,
            RepositorioOrdenCompraProveedor ordenProveedorRepositorio,
            RepositorioDetalleOrdenCompraProveedor detalleOrdenProveedorRepositorio,
            RepositorioFacturaProveedor facturaProveedorRepositorio,
            RepositorioDetalleFactura detalleFacturaRepositorio,
            RepositorioStock stockRepositorio,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.password:Zero2026!}") String claveDemo) {
        this.paisRepositorio = paisRepositorio;
        this.provinciaRepositorio = provinciaRepositorio;
        this.departamentoRepositorio = departamentoRepositorio;
        this.localidadRepositorio = localidadRepositorio;
        this.direccionRepositorio = direccionRepositorio;
        this.personaRepositorio = personaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.contactoTelefonicoRepositorio = contactoTelefonicoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.subCategoriaRepositorio = subCategoriaRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.vigenciaPrecioRepositorio = vigenciaPrecioRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.formaDePagoRepositorio = formaDePagoRepositorio;
        this.ordenProveedorRepositorio = ordenProveedorRepositorio;
        this.detalleOrdenProveedorRepositorio = detalleOrdenProveedorRepositorio;
        this.facturaProveedorRepositorio = facturaProveedorRepositorio;
        this.detalleFacturaRepositorio = detalleFacturaRepositorio;
        this.stockRepositorio = stockRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.claveDemo = claveDemo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Sucursal sucursalPrincipal = asegurarSucursalPrincipal();
        asegurarObjetivosExistentes(sucursalPrincipal);
        if (usuarioRepositorio.count() > 0) {
            log.info("Se encontraron datos existentes; se omite la carga de datos demo");
            return;
        }

        Localidad localidad = crearUbicacion();
        crearUsuario("admin@zero.local", "Admin", "Zero", "30111222", "EMPRESA",
                "2615550101", RolUsuario.ADMINISTRATIVO, localidad);
        crearUsuario("cliente@zero.local", "Alex", "Deportista", "40123456", "NO_BINARIO",
                "2615550102", RolUsuario.CLIENTE, localidad);

        Map<String, Categoria> categorias = crearCategorias();
        List<Producto> productos = crearProductos(categorias, sucursalPrincipal);
        Proveedor proveedor = crearProveedor();
        FormaDePago formaDePago = crearFormaDePago();
        crearCompraProveedorYStock(productos, proveedor, formaDePago, sucursalPrincipal);

        log.info("Datos demo listos: admin@zero.local, cliente@zero.local y {} productos. "
                + "La clave demo se obtiene de APP_DEMO_PASSWORD.", productos.size());
    }

    private Sucursal asegurarSucursalPrincipal() {
        return sucursalRepositorio.findFirstByActivaTrueAndPrincipalTrueOrderByNombreAsc()
                .orElseGet(() -> {
                    Sucursal sucursal = new Sucursal();
                    sucursal.setNombre("Casa Central");
                    sucursal.setDireccion("Dirección pendiente");
                    sucursal.setActiva(true);
                    sucursal.setPrincipal(true);
                    return sucursalRepositorio.save(sucursal);
                });
    }

    private void asegurarObjetivosExistentes(Sucursal sucursal) {
        for (Producto producto : productoRepositorio.findAll()) {
            if (objetivoReposicionRepositorio.findBySucursal_IdAndProducto_Id(sucursal.getId(), producto.getId())
                    .isPresent()) {
                continue;
            }
            ObjetivoReposicion objetivo = new ObjetivoReposicion();
            objetivo.setSucursal(sucursal);
            objetivo.setProducto(producto);
            objetivo.setCantidadObjetivo(Math.max(producto.getStockMaximo(), 0));
            objetivo.setCantidadActual(stockRepositorio
                    .findTopByDetalleFactura_Producto_IdAndEliminadoFalseOrderByFechaMovimientoDesc(producto.getId())
                    .map(Stock::getCantActual)
                    .orElse(0));
            objetivoReposicionRepositorio.save(objetivo);
        }
    }

    private Localidad crearUbicacion() {
        Pais pais = new Pais("Argentina");
        paisRepositorio.save(pais);

        Provincia provincia = new Provincia("Mendoza", pais);
        provinciaRepositorio.save(provincia);

        Departamento departamento = new Departamento("Capital", provincia);
        departamentoRepositorio.save(departamento);

        Localidad localidad = new Localidad("Ciudad de Mendoza", "5500");
        localidad.setDepartamento(departamento);
        return localidadRepositorio.save(localidad);
    }

    private void crearUsuario(String email, String nombre, String apellido, String documento,
            String sexo, String telefono, RolUsuario rol, Localidad localidad) {
        Persona persona = new Persona(nombre, apellido, fecha(1995, 5, 15), documento, TipoDocumento.DNI);
        persona.setSexo(sexo);
        personaRepositorio.save(persona);

        Direccion direccion = new Direccion();
        direccion.setCalle("San Martín");
        direccion.setNumeracion(rol == RolUsuario.CLIENTE ? "1234" : "1000");
        direccion.setCodigoPostal("5500");
        direccion.setBarrio("Centro");
        direccion.setManzanaPiso("Manzana A");
        direccion.setCasaDepartamento("Casa 1");
        direccion.setLocalidad(localidad);
        direccion.setPersona(persona);
        direccionRepositorio.save(direccion);
        persona.getDirecciones().add(direccion);

        ContactoTelefonico contacto = new ContactoTelefonico(telefono, TipoTelefono.CELULAR,
                TipoContacto.PERSONAL, "Teléfono demo", persona);
        contactoTelefonicoRepositorio.save(contacto);
        persona.getContactos().add(contacto);

        Usuario usuario = new Usuario(email, passwordEncoder.encode(claveDemo), rol);
        usuario.setPersona(persona);
        usuarioRepositorio.save(usuario);
        persona.getUsuarios().add(usuario);
        personaRepositorio.save(persona);
    }

    private Map<String, Categoria> crearCategorias() {
        Map<String, Categoria> resultado = new LinkedHashMap<>();
        for (String nombre : List.of("Niños", "Niñas", "Mujeres", "Hombres")) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setActivo(true);
            categoriaRepositorio.save(categoria);
            resultado.put(nombre, categoria);

            for (String nombreSubcategoria : SUBCATEGORIAS) {
                SubCategoria subCategoria = new SubCategoria();
                subCategoria.setNombre(nombreSubcategoria);
                subCategoria.setActivo(true);
                subCategoria.setCategoria(categoria);
                subCategoriaRepositorio.save(subCategoria);
            }
        }
        return resultado;
    }

    private List<Producto> crearProductos(Map<String, Categoria> categorias, Sucursal sucursal) {
        List<ProductSeed> semillas = List.of(
                new ProductSeed("Niños", "Ropa", "Remera Training", "Remera liviana para entrenar", "10", 18500, true),
                new ProductSeed("Niños", "Calzado", "Zapatilla Sprint", "Zapatilla urbana deportiva", "32", 42900, false),
                new ProductSeed("Niños", "Accesorios", "Gorra Active", "Gorra de ajuste regulable", "Único", 12500, false),
                new ProductSeed("Niñas", "Ropa", "Calza Move", "Calza cómoda de secado rápido", "8", 21000, true),
                new ProductSeed("Niñas", "Calzado", "Zapatilla Run", "Zapatilla liviana para todos los días", "32", 39900, false),
                new ProductSeed("Niñas", "Accesorios", "Botella Sport", "Botella reutilizable", "750 ml", 9800, false),
                new ProductSeed("Mujeres", "Ropa", "Buzo Essential", "Buzo deportivo unisex de algodón", "M", 45900, true),
                new ProductSeed("Mujeres", "Calzado", "Zapatilla Motion", "Zapatilla de entrenamiento", "38", 64500, false),
                new ProductSeed("Mujeres", "Accesorios", "Riñonera Active", "Riñonera con ajuste regulable", "Único", 17900, false),
                new ProductSeed("Hombres", "Ropa", "Short Flex", "Short deportivo de secado rápido", "L", 24500, true),
                new ProductSeed("Hombres", "Calzado", "Zapatilla Pace", "Zapatilla de running", "42", 69900, false),
                new ProductSeed("Hombres", "Accesorios", "Medias Training", "Pack de medias deportivas", "Único", 8900, false));

        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < semillas.size(); i++) {
            ProductSeed semilla = semillas.get(i);
            Categoria categoria = categorias.get(semilla.categoria());
            SubCategoria subCategoria = subCategoriaRepositorio
                    .findByCategoria_IdAndNombre(categoria.getId(), semilla.subcategoria())
                    .orElseThrow();

            Producto producto = new Producto();
            producto.setCodigo("ZERO-%03d".formatted(i + 1));
            producto.setNombre(semilla.nombre());
            producto.setDescripcion(semilla.descripcion());
            producto.setTalle(semilla.talle());
            producto.setEnOferta(semilla.enOferta());
            producto.setStockMaximo(20);
            producto.setEliminado(false);
            producto.setSubCategoria(subCategoria);
            productoRepositorio.save(producto);

            ObjetivoReposicion objetivo = new ObjetivoReposicion();
            objetivo.setSucursal(sucursal);
            objetivo.setProducto(producto);
            objetivo.setCantidadObjetivo(producto.getStockMaximo());
            objetivo.setCantidadActual(0);
            objetivoReposicionRepositorio.save(objetivo);

            VigenciaPrecio precio = new VigenciaPrecio();
            precio.setFechaDesde(LocalDate.now().minusDays(1));
            precio.setPrecio(semilla.precio());
            precio.setProducto(producto);
            precio.setEliminado(false);
            vigenciaPrecioRepositorio.save(precio);
            productos.add(producto);
        }
        return productos;
    }

    private Proveedor crearProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Distribuidora Zero Demo");
        proveedor.setEliminado(false);
        proveedorRepositorio.save(proveedor);

        ContactoTelefonico telefono = new ContactoTelefonico("2615550199", TipoTelefono.CELULAR,
                TipoContacto.EMPRESA, "WhatsApp proveedor demo", proveedor);
        contactoTelefonicoRepositorio.save(telefono);
        return proveedor;
    }

    private FormaDePago crearFormaDePago() {
        FormaDePago formaDePago = new FormaDePago();
        formaDePago.setTipoPago(TipoPago.MERCADO_PAGO);
        formaDePago.setObservacion("Pago de proveedores demo");
        formaDePago.setEliminado(false);
        return formaDePagoRepositorio.save(formaDePago);
    }

        private void crearCompraProveedorYStock(List<Producto> productos, Proveedor proveedor,
            FormaDePago formaDePago, Sucursal sucursal) {
        OrdenCompraProveedor orden = new OrdenCompraProveedor();
        orden.setFecha(new Date());
        orden.setEstado(EstadoOrdenCompraProveedor.ENTREGADA);
        orden.setProveedor(proveedor);
        orden.setSucursal(sucursal);
        orden.setEliminado(false);
            orden.setTotal(0);
        ordenProveedorRepositorio.save(orden);

        double totalFactura = 0;
        List<DetalleOrdenCompraProveedor> detallesProveedor = new ArrayList<>();
        List<Double> costos = new ArrayList<>();
        for (Producto producto : productos) {
            double costo = vigenciaPrecioRepositorio.findByProducto_IdAndFechaHastaIsNull(producto.getId())
                    .orElseThrow().getPrecio() * 0.45;
            costos.add(costo);
            totalFactura += costo * 20;
        }
        orden.setTotal(totalFactura);
        ordenProveedorRepositorio.save(orden);

        FacturaProveedor factura = new FacturaProveedor();
        factura.setNumeroFactura(991001L);
        factura.setFechaFactura(new Date());
        factura.setEstadoFactura(EstadoFactura.PAGADA);
        factura.setTotalPagado(totalFactura);
        factura.setFormaDePago(formaDePago);
        factura.setEliminado(false);
        factura.setDetalleFactura(new ArrayList<>());
        factura.setProveedor(proveedor);
        factura.setOrdenCompraProveedor(orden);
        facturaProveedorRepositorio.save(factura);

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            double costo = costos.get(i);

            DetalleFactura detalleFactura = new DetalleFactura();
            detalleFactura.setCantidad(20);
            detalleFactura.setSubtotal(costo * 20);
            detalleFactura.setEliminado(false);
            detalleFactura.setProducto(producto);
            detalleFactura.setFactura(factura);
            detalleFacturaRepositorio.save(detalleFactura);
            factura.getDetalleFactura().add(detalleFactura);

            Stock stock = new Stock();
            stock.setCantActual(20);
            stock.setDetalleFactura(detalleFactura);
            stock.setEliminado(false);
            stock.setObservacion("Stock inicial demo");
            stockRepositorio.save(stock);

                ObjetivoReposicion objetivo = objetivoReposicionRepositorio
                    .findBySucursal_IdAndProducto_Id(sucursal.getId(), producto.getId())
                    .orElseThrow();
                objetivo.setCantidadActual(20);
                objetivoReposicionRepositorio.save(objetivo);

            DetalleOrdenCompraProveedor detalleProveedor = new DetalleOrdenCompraProveedor();
            detalleProveedor.setCantidad(20);
            detalleProveedor.setPrecioCompra(costo);
            detalleProveedor.setEliminado(false);
            detalleProveedor.setOrdenCompraProveedor(orden);
            detalleProveedor.setProducto(producto);
            detalleOrdenProveedorRepositorio.save(detalleProveedor);
            detallesProveedor.add(detalleProveedor);
        }
        facturaProveedorRepositorio.save(factura);
    }

    private Date fecha(int anio, int mes, int dia) {
        return Date.from(LocalDate.of(anio, mes, dia)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private record ProductSeed(String categoria, String subcategoria, String nombre,
            String descripcion, String talle, double precio, boolean enOferta) {
    }
}