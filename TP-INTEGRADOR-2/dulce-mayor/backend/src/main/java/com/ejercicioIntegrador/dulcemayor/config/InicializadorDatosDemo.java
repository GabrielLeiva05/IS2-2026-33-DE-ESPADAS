package com.ejercicioIntegrador.dulcemayor.config;

import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ClienteResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DepartamentoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.DireccionRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.EmpleadoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ImagenResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LineaPedidoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.AsignacionPedidosDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PaisResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PedidoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoVentaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.StockRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.VigenciaPrecioRequestDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.CondicionIva;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCliente;
import com.ejercicioIntegrador.dulcemayor.service.ServicioDepartamento;
import com.ejercicioIntegrador.dulcemayor.service.ServicioEmpleado;
import com.ejercicioIntegrador.dulcemayor.service.ServicioImagen;
import com.ejercicioIntegrador.dulcemayor.service.ServicioLocalidad;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPais;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPedido;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProvincia;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPuntoVenta;
import com.ejercicioIntegrador.dulcemayor.service.ServicioStock;
import com.ejercicioIntegrador.dulcemayor.service.ServicioSubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.ServicioVigenciaPrecio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * DATOS DE DEMOSTRACIÓN para probar el sistema y los reportes (clientes y pedidos, histórico de ventas).
 *
 * Se carga al iniciar si app.datos-demo.habilitado=true (valor por defecto en desarrollo; el perfil prod lo apaga)
 * y DESPUÉS de InicializadorDatos (ubicaciones y jefe). Carga, siempre a través de los servicios y por lo tanto con
 * sus mismas validaciones:
 *  - categorías, subcategorías, productos (cada uno con su imagen), precio vigente y stock abundante;
 *  - un administrativo, preventistas por zona y distribuidores;
 *  - clientes y comercios (puntos de venta) repartidos en los 18 departamentos;
 *  - pedidos en distintos estados distribuidos en los últimos 120 días.
 * Es idempotente: cada elemento se busca por su clave natural (código, legajo, correo, CUIT) y solo se crea si falta.
 * Los pedidos se cargan una única vez (si el primer preventista ya tiene pedidos, no se vuelven a crear).
 *
 * Excepción deliberada a "todo por los servicios": los servicios siempre fechan los pedidos con la hora actual, por lo
 * que, una vez creados y llevados a su estado por los servicios, solo sus fechas (y el estado ENTREGADO, que exige la
 * ruta de entregas del distribuidor) se ajustan con un UPDATE acotado. Esto existe únicamente en este modo demo.
 *
 * Cuentas: los empleados ingresan con la clave temporal "dulcemayor" (deben cambiarla); los clientes, con la clave
 * indicada en DatosDemo.CLAVE_CLIENTES.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.datos-demo.habilitado", havingValue = "true")
public class InicializadorDatosDemo implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorDatosDemo.class);

    private static final int[] PESOS_CUIT = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final ServicioPais servicioPais;
    private final ServicioProvincia servicioProvincia;
    private final ServicioDepartamento servicioDepartamento;
    private final ServicioLocalidad servicioLocalidad;
    private final ServicioCategoria servicioCategoria;
    private final ServicioSubCategoria servicioSubCategoria;
    private final ServicioImagen servicioImagen;
    private final ServicioProducto servicioProducto;
    private final ServicioVigenciaPrecio servicioVigenciaPrecio;
    private final ServicioStock servicioStock;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioCliente servicioCliente;
    private final ServicioPuntoVenta servicioPuntoVenta;
    private final ServicioPedido servicioPedido;
    private final TransactionTemplate transacciones;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.datos-demo.pedidos:120}")
    private int cantidadPedidos;

    public InicializadorDatosDemo(ServicioPais servicioPais, ServicioProvincia servicioProvincia,
                                  ServicioDepartamento servicioDepartamento, ServicioLocalidad servicioLocalidad,
                                  ServicioCategoria servicioCategoria, ServicioSubCategoria servicioSubCategoria,
                                  ServicioImagen servicioImagen, ServicioProducto servicioProducto,
                                  ServicioVigenciaPrecio servicioVigenciaPrecio, ServicioStock servicioStock,
                                  ServicioEmpleado servicioEmpleado, ServicioCliente servicioCliente,
                                  ServicioPuntoVenta servicioPuntoVenta, ServicioPedido servicioPedido,
                                  PlatformTransactionManager administradorTransacciones) {
        this.servicioPais = servicioPais;
        this.servicioProvincia = servicioProvincia;
        this.servicioDepartamento = servicioDepartamento;
        this.servicioLocalidad = servicioLocalidad;
        this.servicioCategoria = servicioCategoria;
        this.servicioSubCategoria = servicioSubCategoria;
        this.servicioImagen = servicioImagen;
        this.servicioProducto = servicioProducto;
        this.servicioVigenciaPrecio = servicioVigenciaPrecio;
        this.servicioStock = servicioStock;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioCliente = servicioCliente;
        this.servicioPuntoVenta = servicioPuntoVenta;
        this.servicioPedido = servicioPedido;
        this.transacciones = new TransactionTemplate(administradorTransacciones);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Map<String, String> localidades = localidadesPorDepartamento();
            if (localidades.isEmpty()) {
                LOG.warn("Datos de demostración omitidos: todavía no están cargadas las ubicaciones de Mendoza");
                return;
            }
            Map<String, String> idsProductos = cargarCatalogo();
            Equipo equipo = cargarEmpleados(localidades);
            List<Comercio> comercios = cargarClientesYComercios(localidades);
            cargarPedidos(equipo, comercios, idsProductos);
        } catch (Exception e) {
            LOG.error("No se pudieron cargar los datos de demostración: {}", e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ Ubicaciones

    /** Departamento de Mendoza -> id de su localidad cabecera (la que carga InicializadorDatos). */
    private Map<String, String> localidadesPorDepartamento() throws MiException {
        Map<String, String> localidades = new LinkedHashMap<>();
        PaisResponseDTO argentina;
        try {
            argentina = servicioPais.buscarPaisPorNombre("Argentina");
        } catch (RecursoNoEncontradoException e) {
            return localidades;
        }
        for (ProvinciaResponseDTO provincia : servicioProvincia.listarProvinciaActivo(argentina.id())) {
            if (!provincia.nombre().equalsIgnoreCase("Mendoza")) {
                continue;
            }
            for (DepartamentoResponseDTO departamento : servicioDepartamento.listarDepartamentoActivo(provincia.id())) {
                List<LocalidadResponseDTO> lista = servicioLocalidad.listarLocalidadActivo(departamento.id());
                if (!lista.isEmpty()) {
                    localidades.put(departamento.nombre(), lista.get(0).id());
                }
            }
        }
        return localidades;
    }

    // ------------------------------------------------------------------ Catálogo

    /** Devuelve código de producto -> id. */
    private Map<String, String> cargarCatalogo() throws MiException {
        Map<String, String> idsCategorias = new LinkedHashMap<>();
        Map<String, String> idsSubCategorias = new LinkedHashMap<>();
        for (Map.Entry<String, String> entrada : DatosDemo.SUBCATEGORIAS.entrySet()) {
            String categoria = entrada.getValue();
            if (!idsCategorias.containsKey(categoria)) {
                idsCategorias.put(categoria, buscarOCrearCategoria(categoria));
            }
            String idCategoria = idsCategorias.get(categoria);
            idsSubCategorias.put(entrada.getKey(), buscarOCrearSubCategoria(entrada.getKey(), idCategoria));
        }

        Map<String, String> idsProductos = new LinkedHashMap<>();
        int creados = 0;
        int indice = 0;
        for (String fila : DatosDemo.PRODUCTOS) {
            String[] c = fila.split("\\|");
            String codigo = c[0];
            String idProducto = idProductoExistente(codigo);
            if (idProducto == null) {
                idProducto = crearProducto(c, indice, idsSubCategorias.get(c[3]));
                creados++;
            }
            idsProductos.put(codigo, idProducto);
            indice++;
        }
        LOG.info("Datos de demostración - catálogo: {} productos nuevos ({} en total)", creados, idsProductos.size());
        return idsProductos;
    }

    private String crearProducto(String[] c, int indice, String idSubCategoria) throws MiException {
        ImagenResponseDTO imagen = servicioImagen.alta(new ImagenRequestDTO(c[0] + ".svg", "image/svg+xml",
                ImagenesDemo.svg(c[1], c[3], indice), TipoImagen.PRODUCTO));
        ProductoResponseDTO producto = servicioProducto.alta(new ProductoRequestDTO(c[0], c[1], c[2],
                Presentacion.valueOf(c[4]), Integer.valueOf(c[5]), "S".equals(c[6]), idSubCategoria,
                List.of(imagen.id())));
        servicioVigenciaPrecio.alta(new VigenciaPrecioRequestDTO(LocalDate.now().minusMonths(6), null,
                Double.valueOf(c[7]), producto.id()));
        int[] especial = DatosDemo.STOCK_ESPECIAL.get(c[0]);
        int cantidad = especial != null ? especial[0] : 400 + (indice * 137) % 2200;
        int minimo = especial != null ? especial[1] : 40 + (indice * 13) % 60;
        servicioStock.alta(new StockRequestDTO(producto.id(), cantidad, minimo));
        return producto.id();
    }

    private String idProductoExistente(String codigo) throws MiException {
        try {
            return servicioProducto.buscarProductoPorCodigo(codigo).id();
        } catch (RecursoNoEncontradoException e) {
            return null;
        }
    }

    private String buscarOCrearCategoria(String nombre) throws MiException {
        try {
            return servicioCategoria.buscarCategoriaPorNombre(nombre).id();
        } catch (RecursoNoEncontradoException e) {
            CategoriaResponseDTO creada = servicioCategoria.alta(new CategoriaRequestDTO(nombre));
            return creada.id();
        }
    }

    private String buscarOCrearSubCategoria(String nombre, String idCategoria) throws MiException {
        for (SubCategoriaResponseDTO existente : servicioSubCategoria.listarSubCategoriaActivo(idCategoria)) {
            if (existente.nombre().equalsIgnoreCase(nombre)) {
                return existente.id();
            }
        }
        return servicioSubCategoria.alta(new SubCategoriaRequestDTO(nombre, idCategoria)).id();
    }

    // ------------------------------------------------------------------ Empleados

    private Equipo cargarEmpleados(Map<String, String> localidades) throws MiException {
        int creados = 0;
        String[] a = DatosDemo.ADMINISTRATIVO.split("\\|");
        creados += buscarOCrearEmpleado(a[3], new EmpleadoRequestDTO(a[0], a[1], LocalDate.of(1984, 5, 12),
                TipoDocumento.DNI, a[2], correo(a[0], a[1]), RolUsuario.ADMINISTRATIVO, a[3],
                LocalDate.now().minusYears(3), direccion(a[4], a[5], a[6], localidades), null, null, null)).creado;

        List<EmpleadoResponseDTO> preventistas = new ArrayList<>();
        int i = 0;
        for (String fila : DatosDemo.PREVENTISTAS) {
            String[] c = fila.split("\\|");
            Resultado<EmpleadoResponseDTO> r = buscarOCrearEmpleado(c[3], new EmpleadoRequestDTO(c[0], c[1],
                    LocalDate.of(1986 + i, 2 + i, 10 + i), TipoDocumento.DNI, c[2], correo(c[0], c[1]),
                    RolUsuario.PREVENTISTA, c[3], LocalDate.now().minusYears(2), direccion(c[5], c[6], c[7], localidades),
                    null, null, null));
            preventistas.add(r.valor);
            creados += r.creado;
            i++;
        }

        List<EmpleadoResponseDTO> distribuidores = new ArrayList<>();
        i = 0;
        for (String fila : DatosDemo.DISTRIBUIDORES) {
            String[] c = fila.split("\\|");
            Resultado<EmpleadoResponseDTO> r = buscarOCrearEmpleado(c[3], new EmpleadoRequestDTO(c[0], c[1],
                    LocalDate.of(1982 + i, 6 + i, 15 + i), TipoDocumento.DNI, c[2], correo(c[0], c[1]),
                    RolUsuario.DISTRIBUIDOR, c[3], LocalDate.now().minusYears(1), direccion(c[6], c[7], c[8], localidades),
                    c[4], c[5], null));
            distribuidores.add(r.valor);
            creados += r.creado;
            i++;
        }
        LOG.info("Datos de demostración - empleados: {} nuevos (clave temporal \"dulcemayor\")", creados);
        return new Equipo(preventistas, distribuidores);
    }

    private Resultado<EmpleadoResponseDTO> buscarOCrearEmpleado(String legajo, EmpleadoRequestDTO dto) throws MiException {
        try {
            return new Resultado<>(servicioEmpleado.buscarEmpleadoPorLegajo(legajo), 0);
        } catch (RecursoNoEncontradoException e) {
            return new Resultado<>(servicioEmpleado.alta(dto), 1);
        }
    }

    private static DireccionRequestDTO direccion(String calle, String numero, String departamento,
                                                 Map<String, String> localidades) {
        return new DireccionRequestDTO(calle, numero, null, null, null, null, null, null, localidades.get(departamento));
    }

    // ------------------------------------------------------------------ Clientes y comercios

    private List<Comercio> cargarClientesYComercios(Map<String, String> localidades) throws MiException {
        Map<Integer, ClienteResponseDTO> clientes = new LinkedHashMap<>();
        int clientesNuevos = 0;
        for (int i = 0; i < DatosDemo.CLIENTES.size(); i++) {
            String[] c = DatosDemo.CLIENTES.get(i).split("\\|");
            String correo = correo(c[0], c[1]);
            try {
                clientes.put(i, servicioCliente.buscarClientePorCorreo(correo));
            } catch (RecursoNoEncontradoException e) {
                clientes.put(i, servicioCliente.alta(new ClienteRequestDTO(c[0], c[1],
                        LocalDate.of(1965 + i, 1 + i % 12, 1 + i % 27), TipoDocumento.DNI,
                        String.valueOf(21000000 + i * 1111111), correo, DatosDemo.CLAVE_CLIENTES, null)));
                clientesNuevos++;
            }
        }

        List<Comercio> comercios = new ArrayList<>();
        int comerciosNuevos = 0;
        for (int i = 0; i < DatosDemo.COMERCIOS.size(); i++) {
            String[] c = DatosDemo.COMERCIOS.get(i).split("\\|");
            ClienteResponseDTO cliente = clientes.get(Integer.parseInt(c[0]));
            String cuit = generarCuit(i);
            String idPuntoVenta;
            try {
                idPuntoVenta = servicioPuntoVenta.buscarPuntoVentaPorCuit(cuit).id();
            } catch (RecursoNoEncontradoException e) {
                double[] centro = DatosDemo.DEPARTAMENTOS.get(c[4]);
                double desplazamiento = ((i * 37) % 21 - 10) * 0.0012;
                PuntoVentaResponseDTO creado = servicioPuntoVenta.alta(new PuntoVentaRequestDTO(c[1], c[2], cuit,
                        condicionIva(c[3]), cliente.id(), new DireccionRequestDTO(c[5], c[6], null, null, null, null,
                        centro[0] + desplazamiento, centro[1] - desplazamiento, localidades.get(c[4]))));
                idPuntoVenta = creado.id();
                comerciosNuevos++;
            }
            comercios.add(new Comercio(idPuntoVenta, c[4], cliente.correo()));
        }
        LOG.info("Datos de demostración - clientes: {} nuevos (clave \"{}\"); comercios: {} nuevos",
                clientesNuevos, DatosDemo.CLAVE_CLIENTES, comerciosNuevos);
        return comercios;
    }

    private static CondicionIva condicionIva(String codigo) {
        return switch (codigo) {
            case "R" -> CondicionIva.RESPONSABLE_INSCRIPTO;
            case "E" -> CondicionIva.EXENTO;
            default -> CondicionIva.MONOTRIBUTISTA;
        };
    }

    /** CUIT de 11 dígitos con dígito verificador válido (mismo algoritmo que ServicioPuntoVenta), distinto por comercio. */
    private static String generarCuit(int orden) {
        int n = orden * 100;
        while (true) {
            String base = "30" + String.format("%08d", 41000000 + n * 7919);
            int suma = 0;
            for (int i = 0; i < 10; i++) {
                suma += (base.charAt(i) - '0') * PESOS_CUIT[i];
            }
            int verificador = 11 - (suma % 11);
            if (verificador == 11) {
                verificador = 0;
            }
            if (verificador != 10) {
                return base + verificador;
            }
            n++;
        }
    }

    // ------------------------------------------------------------------ Pedidos

    private void cargarPedidos(Equipo equipo, List<Comercio> comercios, Map<String, String> idsProductos)
            throws MiException {
        if (cantidadPedidos <= 0) {
            return;
        }
        if (!servicioPedido.listarPedidosDelPreventista(equipo.preventistas.get(0).correo()).isEmpty()) {
            LOG.info("Datos de demostración - pedidos: ya existen, no se vuelven a cargar");
            return;
        }
        List<String> ordenables = new ArrayList<>();
        for (String codigo : idsProductos.keySet()) {
            if (!DatosDemo.STOCK_ESPECIAL.containsKey(codigo)) {
                ordenables.add(codigo);
            }
        }

        Random azar = new Random(20260);
        List<PlanPedido> plan = new ArrayList<>();
        for (int i = 0; i < cantidadPedidos; i++) {
            int edad = (int) (120 * Math.pow(azar.nextDouble(), 1.5));
            Comercio comercio = comercios.get(azar.nextInt(comercios.size()));
            int zona = zonaDe(comercio.departamento);
            int zonaPreventista = azar.nextInt(100) < 12 ? azar.nextInt(equipo.preventistas.size()) : zona;
            List<LineaPedidoDTO> lineas = new ArrayList<>();
            Set<String> usados = new HashSet<>();
            int cantidadLineas = 2 + azar.nextInt(5);
            while (lineas.size() < cantidadLineas) {
                String codigo = ordenables.get(azar.nextInt(ordenables.size()));
                if (usados.add(codigo)) {
                    lineas.add(new LineaPedidoDTO(idsProductos.get(codigo), 1 + azar.nextInt(10)));
                }
            }
            plan.add(new PlanPedido(edad, comercio, zona, zonaPreventista, lineas, destinoSegunEdad(edad, azar.nextInt(100))));
        }
        // Los más antiguos primero: así el número de pedido crece con el tiempo.
        plan.sort(Comparator.comparingInt((PlanPedido p) -> p.edad).reversed());

        Map<Destino, Integer> resumen = new EnumMap<>(Destino.class);
        int fallidos = 0;
        for (PlanPedido p : plan) {
            try {
                ejecutar(p, equipo, azar);
                resumen.merge(p.destino, 1, Integer::sum);
            } catch (MiException e) {
                fallidos++;
                LOG.warn("Datos de demostración - un pedido no se pudo cargar: {}", e.getMessage());
            }
        }
        LOG.info("Datos de demostración - pedidos creados: {} (fallidos: {})", resumen, fallidos);
    }

    private void ejecutar(PlanPedido p, Equipo equipo, Random azar) throws MiException {
        EmpleadoResponseDTO preventista = equipo.preventistas.get(p.zonaPreventista);
        EmpleadoResponseDTO distribuidor = equipo.distribuidores.get(DatosDemo.DISTRIBUIDOR_POR_ZONA[p.zonaComercio]);
        PedidoResponseDTO pedido = servicioPedido.cargarPedido(preventista.correo(),
                new PedidoRequestDTO(p.comercio.idPuntoVenta, p.lineas));
        String id = pedido.id();
        switch (p.destino) {
            case CARGADO -> {
            }
            case ANULADO -> servicioPedido.anularPedido(p.comercio.correoCliente, id);
            case SIN_ASIGNAR -> servicioPedido.confirmarPedido(p.comercio.correoCliente, id);
            case EN_REPARTO, ENTREGADO -> {
                servicioPedido.confirmarPedido(p.comercio.correoCliente, id);
                servicioPedido.asignarDistribuidor(new AsignacionPedidosDTO(distribuidor.id(), List.of(id)));
            }
        }
        if (p.edad >= 1) {
            reubicarEnElTiempo(id, p.destino, p.edad, azar);
        }
    }

    private static Destino destinoSegunEdad(int edad, int tirada) {
        if (edad >= 5) {
            return tirada < 86 ? Destino.ENTREGADO : Destino.ANULADO;
        }
        if (edad >= 1) {
            if (tirada < 40) {
                return Destino.ENTREGADO;
            }
            if (tirada < 65) {
                return Destino.EN_REPARTO;
            }
            if (tirada < 85) {
                return Destino.SIN_ASIGNAR;
            }
            return tirada < 95 ? Destino.CARGADO : Destino.ANULADO;
        }
        if (tirada < 30) {
            return Destino.CARGADO;
        }
        return tirada < 60 ? Destino.SIN_ASIGNAR : Destino.EN_REPARTO;
    }

    private static int zonaDe(String departamento) {
        return (int) DatosDemo.DEPARTAMENTOS.get(departamento)[2];
    }

    /**
     * Lleva las fechas del pedido al pasado, de forma coherente con su destino (carga, confirmación, despacho y entrega
     * en orden, nunca en el futuro) y marca como ENTREGADO los entregados.
     */
    private void reubicarEnElTiempo(String id, Destino destino, int edad, Random azar) {
        LocalDateTime tope = LocalDateTime.now().minusMinutes(1);
        LocalDateTime carga = LocalDate.now().minusDays(edad).atTime(8 + azar.nextInt(10), azar.nextInt(60));
        LocalDateTime facturacion = null;
        LocalDateTime despacho = null;
        LocalDateTime entrega = null;
        EstadoPedido estado = switch (destino) {
            case CARGADO -> EstadoPedido.CARGADO;
            case ANULADO -> EstadoPedido.ANULADO;
            case SIN_ASIGNAR, EN_REPARTO -> EstadoPedido.PENDIENTE_DE_ENTREGA;
            case ENTREGADO -> EstadoPedido.ENTREGADO;
        };
        if (destino == Destino.SIN_ASIGNAR || destino == Destino.EN_REPARTO || destino == Destino.ENTREGADO) {
            facturacion = antesDe(carga.plusMinutes(20 + azar.nextInt(220)), tope);
        }
        if (destino == Destino.EN_REPARTO || destino == Destino.ENTREGADO) {
            despacho = antesDe(facturacion.plusHours(2 + azar.nextInt(14)), tope);
        }
        if (destino == Destino.ENTREGADO) {
            entrega = antesDe(despacho.plusHours(1 + azar.nextInt(10)), tope);
        }
        actualizarFechas(id, estado, carga, facturacion, despacho, entrega);
    }

    private static LocalDateTime antesDe(LocalDateTime fecha, LocalDateTime tope) {
        return fecha.isAfter(tope) ? tope : fecha;
    }

    private void actualizarFechas(String id, EstadoPedido estado, LocalDateTime carga, LocalDateTime facturacion,
                                  LocalDateTime despacho, LocalDateTime entrega) {
        transacciones.executeWithoutResult(estadoTransaccion -> {
            StringBuilder jpql = new StringBuilder("UPDATE Pedido p SET p.fechaHoraCarga = :carga, p.estado = :estado");
            if (facturacion != null) {
                jpql.append(", p.fechaFacturacion = :facturacion");
            }
            if (despacho != null) {
                jpql.append(", p.fechaDespacho = :despacho");
            }
            if (entrega != null) {
                jpql.append(", p.fechaEntrega = :entrega");
            }
            jpql.append(" WHERE p.id = :id");
            Query consulta = entityManager.createQuery(jpql.toString());
            consulta.setParameter("carga", carga);
            consulta.setParameter("estado", estado);
            if (facturacion != null) {
                consulta.setParameter("facturacion", facturacion);
            }
            if (despacho != null) {
                consulta.setParameter("despacho", despacho);
            }
            if (entrega != null) {
                consulta.setParameter("entrega", entrega);
            }
            consulta.setParameter("id", id);
            consulta.executeUpdate();
        });
    }

    // ------------------------------------------------------------------ Auxiliares

    /** Correo de demostración a partir del nombre: sin acentos ni espacios. */
    private static String correo(String nombre, String apellido) {
        String texto = Normalizer.normalize(nombre + "." + apellido, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().replace(" ", "");
        return texto + DatosDemo.DOMINIO_CORREO;
    }

    private enum Destino {
        CARGADO, SIN_ASIGNAR, EN_REPARTO, ENTREGADO, ANULADO
    }

    private record Equipo(List<EmpleadoResponseDTO> preventistas, List<EmpleadoResponseDTO> distribuidores) {
    }

    private record Comercio(String idPuntoVenta, String departamento, String correoCliente) {
    }

    private record Resultado<T>(T valor, int creado) {
    }

    private static final class PlanPedido {
        private final int edad;
        private final Comercio comercio;
        private final int zonaComercio;
        private final int zonaPreventista;
        private final List<LineaPedidoDTO> lineas;
        private final Destino destino;

        private PlanPedido(int edad, Comercio comercio, int zonaComercio, int zonaPreventista,
                           List<LineaPedidoDTO> lineas, Destino destino) {
            this.edad = edad;
            this.comercio = comercio;
            this.zonaComercio = zonaComercio;
            this.zonaPreventista = zonaPreventista;
            this.lineas = lineas;
            this.destino = destino;
        }
    }
}
