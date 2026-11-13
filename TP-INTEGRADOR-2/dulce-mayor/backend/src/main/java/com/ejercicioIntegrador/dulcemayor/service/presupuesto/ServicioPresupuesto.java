package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

import com.ejercicioIntegrador.dulcemayor.dto.MensajePresupuestoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.OpcionChatDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PresupuestoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProductoCatalogoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RenglonPresupuestoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RespuestaChatDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.LimiteExcedidoException;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.ServicioExternoNoDisponibleException;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioCatalogo;
import com.ejercicioIntegrador.dulcemayor.service.ServicioProducto;
import com.ejercicioIntegrador.dulcemayor.service.ServicioPromocion;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ConversorMoneda;
import com.ejercicioIntegrador.dulcemayor.service.cotizacion.ServicioCotizacion;
import com.ejercicioIntegrador.dulcemayor.service.promociones.RenglonDescuento;
import com.ejercicioIntegrador.dulcemayor.service.promociones.ResultadoDescuentos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Asistente de presupuestos del sitio institucional (solo clientes registrados).
 * <p>
 * Reparto de responsabilidades: la IA ({@link ProveedorIA}) solo interpreta el mensaje y devuelve la intención y los
 * nombres que usó el cliente. Este servicio busca esos nombres en el catálogo, pide aclaración si hay varios
 * candidatos, valida cantidades y stock, y calcula precios, promociones automáticas y el total en pesos y dólares
 * con los mismos servicios que usan el catálogo y los pedidos. El asistente no toca la base: el presupuesto vive en
 * memoria y no reserva stock.
 */
@Service
public class ServicioPresupuesto {

    private static final Logger LOG = LoggerFactory.getLogger(ServicioPresupuesto.class);
    private static final int MAXIMO_BULTOS = 999;
    private static final String AYUDA = "Puedo armarte un presupuesto: decime qué productos y cuántos bultos "
            + "(\"5 cajas de alfajores de maicena\"), pedime que saque o cambie algo, o decime cuánto querés gastar "
            + "y te propongo una compra.";

    private final ProveedorIA proveedorIA;
    private final ServicioCatalogo servicioCatalogo;
    private final ServicioProducto servicioProducto;
    private final ServicioPromocion servicioPromocion;
    private final ServicioCotizacion servicioCotizacion;
    private final int largoMaximo;
    private final int mensajesPorMinuto;
    private final Duration inactividad;
    private final Clock reloj;

    private final Map<String, Conversacion> conversaciones = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> enviosPorUsuario = new ConcurrentHashMap<>();

    @Autowired
    public ServicioPresupuesto(ProveedorIA proveedorIA, ServicioCatalogo servicioCatalogo,
                               ServicioProducto servicioProducto, ServicioPromocion servicioPromocion,
                               ServicioCotizacion servicioCotizacion,
                               @Value("${app.asistente.largo-maximo:500}") int largoMaximo,
                               @Value("${app.asistente.mensajes-por-minuto:8}") int mensajesPorMinuto,
                               @Value("${app.asistente.minutos-inactividad:30}") int minutosInactividad) {
        this(proveedorIA, servicioCatalogo, servicioProducto, servicioPromocion, servicioCotizacion, largoMaximo,
                mensajesPorMinuto, minutosInactividad, Clock.systemUTC());
    }

    ServicioPresupuesto(ProveedorIA proveedorIA, ServicioCatalogo servicioCatalogo, ServicioProducto servicioProducto,
                        ServicioPromocion servicioPromocion, ServicioCotizacion servicioCotizacion, int largoMaximo,
                        int mensajesPorMinuto, int minutosInactividad, Clock reloj) {
        this.proveedorIA = proveedorIA;
        this.servicioCatalogo = servicioCatalogo;
        this.servicioProducto = servicioProducto;
        this.servicioPromocion = servicioPromocion;
        this.servicioCotizacion = servicioCotizacion;
        this.largoMaximo = largoMaximo;
        this.mensajesPorMinuto = mensajesPorMinuto;
        this.inactividad = Duration.ofMinutes(minutosInactividad);
        this.reloj = reloj;
    }

    /**
     * Procesa un mensaje del cliente.
     *
     * @param usuario nombre del usuario autenticado (dueño de la conversación)
     * @throws LimiteExcedidoException             si superó los mensajes por minuto
     * @throws ServicioExternoNoDisponibleException si la IA no está configurada o no respondió
     * @throws MiException                         si el mensaje está vacío o es demasiado largo
     */
    public RespuestaChatDTO conversar(String usuario, MensajePresupuestoDTO solicitud) throws MiException {
        String mensaje = solicitud == null || solicitud.mensaje() == null ? "" : solicitud.mensaje().strip();
        String elegido = solicitud == null ? null : solicitud.idProductoElegido();
        if (mensaje.isEmpty() && (elegido == null || elegido.isBlank())) {
            throw new MiException("Escribí un mensaje");
        }
        if (mensaje.length() > largoMaximo) {
            throw new MiException("El mensaje puede tener hasta " + largoMaximo + " caracteres");
        }
        Instant ahora = reloj.instant();
        depurarVencidas(ahora);
        controlarLimite(usuario, ahora);

        Conversacion conversacion = conversacionDe(usuario, solicitud == null ? null : solicitud.idConversacion(), ahora);
        synchronized (conversacion) {
            List<ProductoCatalogoDTO> catalogo = servicioCatalogo.listarProductoConStock(null);
            Map<String, ProductoCatalogoDTO> porId = catalogo.stream()
                    .collect(Collectors.toMap(ProductoCatalogoDTO::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
            Respuesta respuesta = new Respuesta();

            if (!resolverPendiente(conversacion, mensaje, elegido, porId, respuesta)) {
                if (mensaje.isEmpty()) {
                    throw new MiException("Esa opción ya no está disponible; escribí de nuevo qué producto querés");
                }
                InterpretacionIA interpretacion = interpretar(conversacion, mensaje, porId);
                conversacion.pendiente(null);
                aplicar(interpretacion, conversacion, catalogo, respuesta);
            }

            PresupuestoDTO presupuesto = calcular(conversacion, porId);
            String texto = respuesta.texto(presupuesto);
            conversacion.recordar(mensaje.isEmpty() ? "(eligió una opción)" : mensaje, texto, ahora);
            return new RespuestaChatDTO(conversacion.id(), texto, respuesta.opciones, presupuesto);
        }
    }

    /** Cierra la conversación (botón "Nuevo presupuesto" o al salir). Solo el dueño puede cerrarla. */
    public void cerrar(String usuario, String idConversacion) {
        Conversacion conversacion = conversaciones.get(idConversacion);
        if (conversacion != null && conversacion.usuario().equals(usuario)) {
            conversaciones.remove(idConversacion);
        }
    }

    // ---------------------------------------------------------------- conversación y límites

    private Conversacion conversacionDe(String usuario, String id, Instant ahora) {
        if (id != null) {
            Conversacion existente = conversaciones.get(id);
            // Una conversación ajena se trata como inexistente (no se revela que existe).
            if (existente != null && existente.usuario().equals(usuario)) {
                return existente;
            }
        }
        // Una conversación activa por cliente: la nueva reemplaza a la anterior.
        conversaciones.values().removeIf(c -> c.usuario().equals(usuario));
        Conversacion nueva = new Conversacion(usuario, ahora);
        conversaciones.put(nueva.id(), nueva);
        return nueva;
    }

    private void depurarVencidas(Instant ahora) {
        Instant limite = ahora.minus(inactividad);
        conversaciones.values().removeIf(c -> c.ultimaActividad().isBefore(limite));
        enviosPorUsuario.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(ahora.minusSeconds(60));
            }
        });
    }

    private void controlarLimite(String usuario, Instant ahora) throws LimiteExcedidoException {
        Deque<Instant> envios = enviosPorUsuario.computeIfAbsent(usuario, u -> new ArrayDeque<>());
        synchronized (envios) {
            Instant haceUnMinuto = ahora.minusSeconds(60);
            while (!envios.isEmpty() && !envios.peekFirst().isAfter(haceUnMinuto)) {
                envios.pollFirst();
            }
            if (envios.size() >= mensajesPorMinuto) {
                throw new LimiteExcedidoException("Enviaste muchos mensajes seguidos; esperá un minuto y seguimos");
            }
            envios.addLast(ahora);
        }
    }

    private InterpretacionIA interpretar(Conversacion conversacion, String mensaje, Map<String, ProductoCatalogoDTO> porId)
            throws ServicioExternoNoDisponibleException, LimiteExcedidoException {
        if (!proveedorIA.configurado()) {
            throw new ServicioExternoNoDisponibleException("El asistente de presupuestos no está disponible en este momento");
        }
        try {
            return proveedorIA.interpretar(conversacion.historial(), mensaje, resumen(conversacion, porId));
        } catch (ErrorProveedorIA e) {
            // Se registra el motivo técnico, nunca el mensaje del cliente ni la clave.
            LOG.warn("Asistente de presupuestos: {}", e.getMessage());
            if (e.isLimite()) {
                throw new LimiteExcedidoException("El asistente está recibiendo muchas consultas; probá de nuevo en un minuto");
            }
            throw new ServicioExternoNoDisponibleException("El asistente no pudo responder; probá de nuevo en unos minutos");
        }
    }

    private static String resumen(Conversacion conversacion, Map<String, ProductoCatalogoDTO> porId) {
        if (conversacion.items().isEmpty()) {
            return "vacío";
        }
        return conversacion.items().entrySet().stream()
                .map(e -> e.getValue() + " x " + (porId.containsKey(e.getKey()) ? porId.get(e.getKey()).nombre() : "producto no disponible"))
                .collect(Collectors.joining("; "));
    }

    // ---------------------------------------------------------------- acciones

    /** Si había un pedido ambiguo y el cliente eligió una opción (botón o número), lo completa sin llamar a la IA. */
    private boolean resolverPendiente(Conversacion conversacion, String mensaje, String elegido,
                                      Map<String, ProductoCatalogoDTO> porId, Respuesta respuesta) {
        Conversacion.Pendiente pendiente = conversacion.pendiente();
        if (pendiente == null) {
            return false;
        }
        String id = null;
        if (elegido != null && pendiente.idsProducto().contains(elegido)) {
            id = elegido;
        } else if (mensaje.matches("\\d{1,2}")) {
            int numero = Integer.parseInt(mensaje);
            if (numero >= 1 && numero <= pendiente.idsProducto().size()) {
                id = pendiente.idsProducto().get(numero - 1);
            }
        }
        if (id == null || !porId.containsKey(id)) {
            return false;
        }
        conversacion.pendiente(null);
        aplicarA(pendiente.accion(), porId.get(id), pendiente.cantidad(), conversacion, respuesta);
        return true;
    }

    private void aplicar(InterpretacionIA interpretacion, Conversacion conversacion, List<ProductoCatalogoDTO> catalogo,
                         Respuesta respuesta) {
        switch (interpretacion.accion()) {
            case AGREGAR, QUITAR, CAMBIAR_CANTIDAD -> {
                if (interpretacion.items().isEmpty()) {
                    respuesta.linea("¿Qué producto querés " + verbo(interpretacion.accion()) + "?");
                    return;
                }
                // Para quitar o cambiar se busca primero entre lo que ya lleva.
                List<ProductoCatalogoDTO> dondeBuscar = interpretacion.accion() == AccionPresupuesto.AGREGAR
                        ? catalogo
                        : catalogo.stream().filter(p -> conversacion.items().containsKey(p.id())).toList();
                for (ItemSolicitado item : interpretacion.items()) {
                    BuscadorProductos.Resultado resultado = BuscadorProductos.buscar(item.producto(), dondeBuscar);
                    if (resultado.encontrado()) {
                        aplicarA(interpretacion.accion(), resultado.unico(), item.cantidad(), conversacion, respuesta);
                    } else if (resultado.ambiguo() && respuesta.opciones.isEmpty()) {
                        conversacion.pendiente(new Conversacion.Pendiente(interpretacion.accion(), item.cantidad(),
                                resultado.candidatos().stream().map(ProductoCatalogoDTO::id).toList()));
                        respuesta.linea("Encontré varios productos para \"" + item.producto() + "\". ¿Cuál querés? "
                                + "Elegí una opción o respondé con el número.");
                        int numero = 1;
                        for (ProductoCatalogoDTO candidato : resultado.candidatos()) {
                            respuesta.opciones.add(new OpcionChatDTO(candidato.id(), numero++ + ". " + candidato.nombre(),
                                    presentacion(candidato), precioEfectivo(candidato)));
                        }
                    } else if (resultado.ambiguo()) {
                        respuesta.linea("También mencionaste \"" + item.producto() + "\": cuando elijas, pedímelo de nuevo.");
                    } else if (interpretacion.accion() == AccionPresupuesto.AGREGAR) {
                        respuesta.linea("No encontré \"" + item.producto() + "\" entre los productos disponibles.");
                    } else {
                        respuesta.linea("\"" + item.producto() + "\" no está en tu presupuesto.");
                    }
                }
            }
            case RECOMENDAR -> recomendar(interpretacion, conversacion, catalogo, respuesta);
            case VACIAR -> {
                conversacion.items().clear();
                respuesta.linea("Listo, empezamos un presupuesto nuevo.");
            }
            case CONSULTAR -> respuesta.linea(conversacion.items().isEmpty()
                    ? "Tu presupuesto está vacío. " + AYUDA : "Este es tu presupuesto:");
            case OTRA -> {
                String texto = interpretacion.respuesta() == null ? "" : interpretacion.respuesta().strip();
                // La IA no habla de precios: si igual lo intenta, se descarta su texto.
                if (texto.isEmpty() || texto.length() > 400 || texto.contains("$") || texto.matches("(?s).*\\d.*")) {
                    texto = "¡Hola!";
                }
                respuesta.linea(texto + " " + AYUDA);
            }
        }
    }

    private void aplicarA(AccionPresupuesto accion, ProductoCatalogoDTO producto, Integer cantidad,
                          Conversacion conversacion, Respuesta respuesta) {
        Map<String, Integer> items = conversacion.items();
        int actual = items.getOrDefault(producto.id(), 0);
        switch (accion) {
            case AGREGAR -> {
                int sumar = cantidad == null ? 1 : cantidad;
                if (sumar <= 0) {
                    respuesta.linea("La cantidad de " + producto.nombre() + " tiene que ser mayor que cero.");
                    return;
                }
                items.put(producto.id(), Math.min(actual + sumar, MAXIMO_BULTOS));
                respuesta.linea("Agregué " + bultos(sumar) + " de " + producto.nombre() + ".");
            }
            case CAMBIAR_CANTIDAD -> {
                if (cantidad == null || cantidad < 0) {
                    respuesta.linea("¿Cuántos bultos de " + producto.nombre() + " querés?");
                } else if (cantidad == 0) {
                    items.remove(producto.id());
                    respuesta.linea("Saqué " + producto.nombre() + ".");
                } else {
                    items.put(producto.id(), Math.min(cantidad, MAXIMO_BULTOS));
                    respuesta.linea("Dejé " + bultos(cantidad) + " de " + producto.nombre() + ".");
                }
            }
            case QUITAR -> {
                if (cantidad == null || cantidad >= actual) {
                    items.remove(producto.id());
                    respuesta.linea("Saqué " + producto.nombre() + ".");
                } else if (cantidad > 0) {
                    items.put(producto.id(), actual - cantidad);
                    respuesta.linea("Resté " + bultos(cantidad) + " de " + producto.nombre() + ".");
                }
            }
            default -> {
                // Las otras acciones no se aplican a un producto puntual.
            }
        }
    }

    private void recomendar(InterpretacionIA interpretacion, Conversacion conversacion,
                            List<ProductoCatalogoDTO> catalogo, Respuesta respuesta) {
        BigDecimal monto = interpretacion.presupuesto();
        if (monto == null || monto.signum() <= 0) {
            respuesta.linea("¿Cuánto querés gastar? Decime un monto en pesos y te armo una propuesta.");
            return;
        }
        List<ProductoCatalogoDTO> candidatos = interpretacion.categoria() == null
                ? catalogo : BuscadorProductos.delRubro(interpretacion.categoria(), catalogo);
        if (candidatos.isEmpty()) {
            respuesta.linea("No encontré productos disponibles de \"" + interpretacion.categoria() + "\".");
            return;
        }
        Map<String, Integer> propuesta = RecomendadorPresupuesto.recomendar(candidatos.stream()
                .map(p -> new RecomendadorPresupuesto.Candidato(p.id(), precioEfectivo(p), p.stockDisponible()))
                .toList(), monto);
        if (propuesta.isEmpty()) {
            respuesta.linea("Con " + pesos(monto) + " no alcanza para ningún bulto de esos productos.");
            return;
        }
        conversacion.items().clear();
        conversacion.items().putAll(propuesta);
        respuesta.linea("Te propongo esta compra para no pasarte de " + pesos(monto)
                + " (reemplaza lo que tenías). Podés pedirme que cambie o saque lo que quieras.");
    }

    // ---------------------------------------------------------------- cálculo

    /** Precios y promociones vigentes, con los mismos servicios que usan el catálogo y la carga de pedidos. */
    PresupuestoDTO calcular(Conversacion conversacion, Map<String, ProductoCatalogoDTO> porId) {
        List<String> avisos = new ArrayList<>();
        Map<String, Producto> entidades = new LinkedHashMap<>();
        if (!conversacion.items().isEmpty()) {
            for (Producto producto : servicioProducto.listarEntidadesActivas()) {
                entidades.put(producto.getId(), producto);
            }
        }
        List<ProductoCatalogoDTO> productos = new ArrayList<>();
        List<RenglonDescuento> renglones = new ArrayList<>();
        Iterator<Map.Entry<String, Integer>> it = conversacion.items().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> item = it.next();
            ProductoCatalogoDTO enCatalogo = porId.get(item.getKey());
            Producto entidad = entidades.get(item.getKey());
            if (enCatalogo == null || entidad == null) {
                avisos.add("Un producto dejó de estar disponible y lo saqué del presupuesto.");
                it.remove();
                continue;
            }
            if (item.getValue() > enCatalogo.stockDisponible()) {
                avisos.add("De " + enCatalogo.nombre() + " hay " + bultos(enCatalogo.stockDisponible())
                        + " disponibles; ajusté la cantidad.");
                item.setValue(enCatalogo.stockDisponible());
            }
            productos.add(enCatalogo);
            renglones.add(new RenglonDescuento(entidad, item.getValue(), BigDecimal.valueOf(enCatalogo.precio())));
        }
        ResultadoDescuentos resultado;
        try {
            resultado = servicioPromocion.calcular(renglones, null);
        } catch (MiException e) {
            // Sin código no hay validaciones que puedan fallar; se protege igual el cálculo.
            throw new IllegalStateException(e);
        }
        List<RenglonPresupuestoDTO> lineas = new ArrayList<>();
        for (int i = 0; i < renglones.size(); i++) {
            RenglonDescuento renglon = renglones.get(i);
            ProductoCatalogoDTO producto = productos.get(i);
            BigDecimal descuento = resultado.descuentoDelRenglon(i);
            BigDecimal subtotal = renglon.subtotal();
            lineas.add(new RenglonPresupuestoDTO(producto.id(), producto.nombre(), presentacion(producto),
                    renglon.cantidad(), dinero(renglon.precioUnitario()), subtotal, descuento, subtotal.subtract(descuento)));
        }
        BigDecimal cotizacion = servicioCotizacion.ventaVigente().orElse(null);
        BigDecimal totalUsd = cotizacion == null ? null : ConversorMoneda.aDolares(resultado.total(), cotizacion);
        return new PresupuestoDTO(lineas, resultado.subtotal(), resultado.totalDescuento(), resultado.total(),
                totalUsd, cotizacion, avisos);
    }

    private static BigDecimal precioEfectivo(ProductoCatalogoDTO producto) {
        return producto.precioPromocional() != null ? producto.precioPromocional() : dinero(BigDecimal.valueOf(producto.precio()));
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static String presentacion(ProductoCatalogoDTO producto) {
        if (producto.presentacion() == null) {
            return "";
        }
        String nombre = producto.presentacion().name();
        String texto = nombre.charAt(0) + nombre.substring(1).toLowerCase(Locale.ROOT);
        return producto.unidadesPorPresentacion() > 1 ? texto + " x " + producto.unidadesPorPresentacion() : texto;
    }

    private static String bultos(int cantidad) {
        return cantidad == 1 ? "1 bulto" : cantidad + " bultos";
    }

    private static String verbo(AccionPresupuesto accion) {
        return switch (accion) {
            case QUITAR -> "sacar";
            case CAMBIAR_CANTIDAD -> "cambiar";
            default -> "agregar";
        };
    }

    static String pesos(BigDecimal importe) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-AR"));
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return "$ " + formato.format(importe);
    }

    /** Texto de la respuesta: las líneas de cada acción y, al final, el total calculado por el backend. */
    private static final class Respuesta {

        private final List<String> lineas = new ArrayList<>();
        private final List<OpcionChatDTO> opciones = new ArrayList<>();

        void linea(String texto) {
            lineas.add(texto);
        }

        String texto(PresupuestoDTO presupuesto) {
            List<String> partes = new ArrayList<>(lineas);
            partes.addAll(presupuesto.avisos());
            if (!presupuesto.renglones().isEmpty() && opciones.isEmpty()) {
                String total = "Total estimado sin IVA: " + pesos(presupuesto.total());
                if (presupuesto.descuento().signum() > 0) {
                    total += " (con " + pesos(presupuesto.descuento()) + " de descuento por promociones)";
                }
                if (presupuesto.totalUsd() != null) {
                    total += ", ≈ US$ " + presupuesto.totalUsd().toPlainString();
                }
                partes.add(total + ".");
            }
            return String.join(" ", partes);
        }
    }
}
