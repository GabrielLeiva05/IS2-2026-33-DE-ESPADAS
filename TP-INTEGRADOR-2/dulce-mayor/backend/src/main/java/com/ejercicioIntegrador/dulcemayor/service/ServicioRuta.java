package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PedidoResumenDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PuntoRutaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaDelDiaDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.TrayectoDTO;
import com.ejercicioIntegrador.dulcemayor.dto.UbicacionDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.ResultadoVisita;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.OperacionNoPermitidaException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.model.Preventista;
import com.ejercicioIntegrador.dulcemayor.model.PuntoRuta;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Ruta;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioRuta;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import com.ejercicioIntegrador.dulcemayor.service.rutas.MatrizRecorrido;
import com.ejercicioIntegrador.dulcemayor.service.rutas.MotorRecorridos;
import com.ejercicioIntegrador.dulcemayor.service.rutas.OptimizadorRecorrido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Rutas del día (HU-18, HU-19, HU-20 y la ruta de entregas del distribuidor).
 *
 * - El administrativo arma la ruta de VISITA: preventista, fecha y puntos de venta (una por preventista y día).
 * - La app ordena los puntos PENDIENTES desde la ubicación actual (ordenarRuta del diagrama), igual para el
 *   preventista (visitas) y el distribuidor (entregas): el motor de recorridos (OSRM /table) da los tiempos de
 *   manejo entre todos los puntos y OptimizadorRecorrido decide el orden (exacto hasta 12 paradas, heurístico
 *   con más). Si OSRM no responde se ordena con distancias en línea recta y se informa. Los puntos ya visitados
 *   no se vuelven a ordenar ni se incluyen en el recorrido.
 * - Cada parada guarda su resultado (con pedido, sin pedido, entregado, no entregado) y un motivo.
 * Las rutas de la app se buscan SIEMPRE por el empleado autenticado, nunca por un id recibido.
 */
@Service
public class ServicioRuta extends BaseService<Ruta, RutaRequestDTO, RutaResponseDTO> {

    private final RepositorioRuta repositorioRuta;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioPuntoVenta servicioPuntoVenta;
    private final MotorRecorridos servicioMapas;

    public ServicioRuta(RepositorioRuta repositorioRuta, ServicioEmpleado servicioEmpleado,
                        ServicioPuntoVenta servicioPuntoVenta, MotorRecorridos servicioMapas) {
        super(repositorioRuta);
        this.repositorioRuta = repositorioRuta;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioPuntoVenta = servicioPuntoVenta;
        this.servicioMapas = servicioMapas;
    }

    // ------------------------------------------------------------------
    // Pasos del Template Method
    // ------------------------------------------------------------------

    @Override
    protected Ruta crearEntidad(RutaRequestDTO dto) throws MiException {
        Ruta ruta = new Ruta();
        ruta.setFecha(dto.fecha() == null ? LocalDate.now() : dto.fecha());
        ruta.setTipoRuta(dto.tipoRuta() == null ? TipoRuta.VISITA : dto.tipoRuta());
        ruta.setEmpleado(obtenerReferencia(servicioEmpleado, dto.idEmpleado(), "el empleado"));
        List<String> ids = dto.idsPuntoVenta() == null ? List.of() : dto.idsPuntoVenta();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new MiException("Un punto de venta no puede repetirse en la misma ruta");
        }
        int orden = 1;
        for (String id : ids) {
            ruta.getPuntos().add(new PuntoRuta(obtenerReferencia(servicioPuntoVenta, id, "el punto de venta"), orden++));
        }
        return ruta;
    }

    @Override
    protected void actualizarEntidad(Ruta ruta, RutaRequestDTO dto) throws MiException {
        throw new OperacionNoPermitidaException(
                "Una ruta no se modifica completa: agregá o quitá puntos pendientes");
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Ruta ruta) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            if (ruta.getPuntos().stream().anyMatch(PuntoRuta::isVisitado)) {
                throw new MiException("No se puede dar de baja una ruta que ya tiene visitas registradas");
            }
            return;
        }
        if (casoDeUso == BaseUseCaseService.ALTA && ruta.getFecha().isBefore(LocalDate.now())) {
            throw new MiException("No se puede armar una ruta para una fecha pasada");
        }
        Empleado empleado = ruta.getEmpleado();
        if (ruta.getTipoRuta() == TipoRuta.VISITA && !(empleado instanceof Preventista)) {
            throw new MiException("Las rutas de visita se asignan a un preventista");
        }
        if (ruta.getTipoRuta() == TipoRuta.ENTREGA && !(empleado instanceof Distribuidor)) {
            throw new MiException("Las rutas de entrega se asignan a un distribuidor");
        }
        if (empleado.isEliminado()) {
            throw new MiException("El empleado está dado de baja");
        }
        if (ruta.getPuntos().isEmpty()) {
            throw new MiException("La ruta debe tener al menos un punto de venta");
        }
        Optional<Ruta> mismoDia = repositorioRuta.findFirstByEmpleado_IdAndFechaAndTipoRutaAndEliminadoFalse(
                empleado.getId(), ruta.getFecha(), ruta.getTipoRuta());
        if (mismoDia.isPresent() && !Objects.equals(mismoDia.get().getId(), ruta.getId())) {
            throw new MiException("El empleado ya tiene una ruta de " + ruta.getTipoRuta().name().toLowerCase()
                    + " para el " + ruta.getFecha() + ": agregale puntos a esa ruta");
        }
    }

    @Override
    public RutaResponseDTO convertirAResponse(Ruta ruta) {
        return convertirAResponse(ruta, puntoVenta -> List.of());
    }

    @Override
    protected String nombreEntidad() {
        return "la ruta";
    }

    /** Igual que convertirAResponse, con los pedidos de cada parada (los informa el servicio de pedidos). */
    public RutaResponseDTO convertirAResponse(Ruta ruta, Function<PuntoVenta, List<PedidoResumenDTO>> pedidosDe) {
        List<PuntoRutaDTO> puntos = ruta.getPuntos().stream()
                .sorted(Comparator.comparingInt(PuntoRuta::getOrden))
                .map(p -> aDTO(p, pedidosDe.apply(p.getPuntoVenta())))
                .toList();
        int visitados = (int) ruta.getPuntos().stream().filter(PuntoRuta::isVisitado).count();
        Empleado empleado = ruta.getEmpleado();
        return new RutaResponseDTO(ruta.getId(), ruta.getFecha(), ruta.getTipoRuta(), empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(), puntos, puntos.size() - visitados, visitados,
                ruta.isEliminado(), ruta.getCriterioOrden());
    }

    // ------------------------------------------------------------------
    // Operaciones del administrativo
    // ------------------------------------------------------------------

    /** agregarPunto(idRuta, idPuntoVenta) del diagrama. */
    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO agregarPunto(String idRuta, String idPuntoVenta) throws MiException {
        Ruta ruta = obtenerEntidad(idRuta);
        if (ruta.getFecha().isBefore(LocalDate.now())) {
            throw new MiException("No se puede modificar una ruta de un día anterior");
        }
        PuntoVenta puntoVenta = obtenerReferencia(servicioPuntoVenta, idPuntoVenta, "el punto de venta");
        if (buscarParada(ruta, puntoVenta.getId()).isPresent()) {
            throw new MiException("El punto de venta ya está en la ruta");
        }
        ruta.getPuntos().add(new PuntoRuta(puntoVenta, siguienteOrden(ruta)));
        ruta.setCriterioOrden(null); // el orden ya no es el calculado: la app lo vuelve a optimizar
        return convertirAResponse(repositorioRuta.save(ruta));
    }

    /** quitarPunto(idRuta, idPuntoVenta) del diagrama: solo puntos todavía pendientes. */
    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO quitarPunto(String idRuta, String idPuntoVenta) throws MiException {
        Ruta ruta = obtenerEntidad(idRuta);
        PuntoRuta parada = buscarParada(ruta, idPuntoVenta)
                .orElseThrow(() -> new MiException("El punto de venta no está en la ruta"));
        if (parada.isVisitado()) {
            throw new MiException("No se puede quitar un punto que ya fue visitado");
        }
        if (ruta.getPuntos().size() == 1) {
            throw new MiException("La ruta debe conservar al menos un punto: dala de baja si ya no corresponde");
        }
        ruta.getPuntos().remove(parada);
        ruta.setCriterioOrden(null);
        return convertirAResponse(repositorioRuta.save(ruta));
    }

    @Transactional(readOnly = true)
    public List<RutaResponseDTO> listarRutasDeLaFecha(LocalDate fecha) {
        return convertirAResponse(repositorioRuta.findByFechaAndEliminadoFalse(fecha == null ? LocalDate.now() : fecha));
    }

    // ------------------------------------------------------------------
    // Operaciones de la app (preventista y distribuidor autenticados)
    // ------------------------------------------------------------------

    /** Ruta de visitas de hoy del preventista autenticado (HU-19). */
    @Transactional(readOnly = true)
    public RutaDelDiaDTO miRutaDeVisitas(String correoPreventista, UbicacionDTO ubicacion) throws MiException {
        return rutaDelDia(preventista(correoPreventista), TipoRuta.VISITA, ubicacion, puntoVenta -> List.of());
    }

    /** HU-18: ordena los puntos pendientes de su ruta de hoy desde su ubicación actual. */
    @Transactional(rollbackFor = Exception.class)
    public RutaDelDiaDTO ordenarMiRutaDeVisitas(String correoPreventista, UbicacionDTO inicio) throws MiException {
        return ordenarRuta(preventista(correoPreventista), TipoRuta.VISITA, inicio, puntoVenta -> List.of());
    }

    /** HU-20: visita sin pedido en un punto pendiente de su ruta de hoy. */
    @Transactional(rollbackFor = Exception.class)
    public void registrarVisitaSinPedido(String correoPreventista, String idPuntoVenta, String motivo)
            throws MiException {
        registrarVisitaSinPedido(preventista(correoPreventista), idPuntoVenta, motivo);
    }

    /** buscarRutaDelDia(idEmpleado, fecha) del diagrama, para el empleado autenticado. */
    @Transactional(readOnly = true)
    public Optional<Ruta> buscarRutaDelDia(Empleado empleado, TipoRuta tipo, LocalDate fecha) {
        return repositorioRuta.findFirstByEmpleado_IdAndFechaAndTipoRutaAndEliminadoFalse(empleado.getId(), fecha, tipo);
    }

    /**
     * Ruta de hoy del empleado con el siguiente punto sugerido y el trayecto desde su ubicación
     * (si la informó) por los puntos pendientes en el orden guardado. Solo lectura.
     */
    @Transactional(readOnly = true)
    public RutaDelDiaDTO rutaDelDia(Empleado empleado, TipoRuta tipo, UbicacionDTO ubicacion,
                                    Function<PuntoVenta, List<PedidoResumenDTO>> pedidosDe) throws MiException {
        validarUbicacionOpcional(ubicacion);
        Ruta ruta = buscarRutaDelDia(empleado, tipo, LocalDate.now())
                .orElseThrow(() -> new RecursoNoEncontradoException(tipo == TipoRuta.VISITA
                        ? "No tenés una ruta de visitas asignada para hoy"
                        : "No tenés entregas asignadas para hoy"));
        return armarRutaDelDia(ruta, ubicacion, pedidosDe);
    }

    /**
     * ordenarRuta(idRuta, latitudInicio, longitudInicio) del diagrama: decide el orden de los puntos PENDIENTES
     * desde la ubicación indicada y lo guarda. Los visitados quedan primero, en el orden en que se visitaron.
     */
    @Transactional(rollbackFor = Exception.class)
    public RutaDelDiaDTO ordenarRuta(Empleado empleado, TipoRuta tipo, UbicacionDTO inicio,
                                     Function<PuntoVenta, List<PedidoResumenDTO>> pedidosDe) throws MiException {
        if (inicio == null || inicio.latitud() == null || inicio.longitud() == null) {
            throw new MiException("Indicá la ubicación inicial: activá el GPS o elegí un punto en el mapa");
        }
        validarUbicacionOpcional(inicio);
        Ruta ruta = buscarRutaDelDia(empleado, tipo, LocalDate.now())
                .orElseThrow(() -> new RecursoNoEncontradoException("No tenés una ruta asignada para hoy"));
        List<PuntoRuta> visitados = ruta.getPuntos().stream()
                .filter(PuntoRuta::isVisitado)
                .sorted(Comparator.comparing(PuntoRuta::getFechaHoraVisita,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<PuntoRuta> pendientes = ruta.getPuntos().stream().filter(p -> !p.isVisitado()).toList();
        OrdenCalculado calculado = ordenOptimo(inicio.latitud(), inicio.longitud(), pendientes);
        int orden = 1;
        for (PuntoRuta p : visitados) {
            p.setOrden(orden++);
        }
        for (PuntoRuta p : calculado.orden()) {
            p.setOrden(orden++);
        }
        if (!pendientes.isEmpty()) {
            ruta.setCriterioOrden(calculado.criterio());
        }
        return armarRutaDelDia(repositorioRuta.save(ruta), inicio, pedidosDe);
    }

    /** Orden de los pendientes y la explicación honesta de cómo se calculó. */
    record OrdenCalculado(List<PuntoRuta> orden, String criterio) {
    }

    /** Matriz del motor de recorridos (ubicación = índice 0) + optimizador. */
    OrdenCalculado ordenOptimo(double latitud, double longitud, List<PuntoRuta> pendientes) {
        if (pendientes.size() <= 1) {
            return new OrdenCalculado(pendientes, pendientes.isEmpty() ? null : "única parada pendiente");
        }
        List<double[]> puntos = new ArrayList<>();
        puntos.add(new double[]{latitud, longitud});
        pendientes.forEach(p -> puntos.add(new double[]{coordenada(p, true), coordenada(p, false)}));
        MatrizRecorrido matriz = servicioMapas.matriz(puntos);
        OptimizadorRecorrido.Resultado resultado = OptimizadorRecorrido.optimizar(matriz.costos());
        List<PuntoRuta> orden = resultado.orden().stream().map(i -> pendientes.get(i - 1)).toList();
        String metodo = resultado.exacto()
                ? "Orden óptimo (búsqueda exacta Held-Karp)"
                : "Orden aproximado (vecino más cercano + mejora 2-opt; con más de "
                        + OptimizadorRecorrido.LIMITE_EXACTO + " paradas no se garantiza el óptimo)";
        return new OrdenCalculado(orden, metodo + " por " + matriz.criterio());
    }

    /**
     * HU-20, visita sin pedido: el preventista registra que atendió el comercio pero no hubo venta.
     * Solo se puede marcar un punto PENDIENTE de SU ruta de hoy (no puntos de otros ni ya visitados).
     */
    @Transactional(rollbackFor = Exception.class)
    public void registrarVisitaSinPedido(Empleado preventista, String idPuntoVenta, String motivo) throws MiException {
        Ruta ruta = buscarRutaDelDia(preventista, TipoRuta.VISITA, LocalDate.now())
                .orElseThrow(() -> new RecursoNoEncontradoException("No tenés una ruta de visitas asignada para hoy"));
        registrarResultado(ruta, idPuntoVenta, ResultadoVisita.SIN_PEDIDO, motivo);
    }

    /**
     * marcarPuntoVisitado(idRuta, idPuntoVenta) del diagrama, con el resultado de la visita.
     * Lo usan el servicio de pedidos (con pedido, entregado, no entregado) y la visita sin pedido.
     */
    @Transactional(rollbackFor = Exception.class)
    public void registrarResultado(Ruta ruta, String idPuntoVenta, ResultadoVisita resultado, String motivo)
            throws MiException {
        PuntoRuta parada = buscarParada(ruta, idPuntoVenta)
                .orElseThrow(() -> new MiException("El punto de venta no está en tu ruta de hoy"));
        boolean deVisita = resultado == ResultadoVisita.CON_PEDIDO || resultado == ResultadoVisita.SIN_PEDIDO;
        boolean deEntrega = resultado == ResultadoVisita.ENTREGADO || resultado == ResultadoVisita.NO_ENTREGADO;
        if ((ruta.getTipoRuta() == TipoRuta.VISITA && !deVisita) || (ruta.getTipoRuta() == TipoRuta.ENTREGA && !deEntrega)) {
            throw new MiException("El resultado no corresponde al tipo de ruta");
        }
        // Una visita sin pedido puede pasar a "con pedido" si el preventista vuelve y el comercio compra.
        boolean mejoraDeVisita = parada.getResultado() == ResultadoVisita.SIN_PEDIDO
                && resultado == ResultadoVisita.CON_PEDIDO;
        if (parada.isVisitado() && !mejoraDeVisita) {
            throw new MiException("Ese punto ya fue atendido hoy");
        }
        String motivoLimpio = estaVacio(motivo) ? null : motivo.trim();
        if (motivoLimpio != null && motivoLimpio.length() > 255) {
            throw new MiException("El motivo no puede superar los 255 caracteres");
        }
        if (resultado == ResultadoVisita.NO_ENTREGADO && motivoLimpio == null) {
            throw new MiException("Indicá el motivo por el que no se pudo entregar");
        }
        parada.marcarVisitado(resultado, mejoraDeVisita ? parada.getMotivo() : motivoLimpio, LocalDateTime.now());
        repositorioRuta.save(ruta);
    }

    /**
     * Ruta de ENTREGA del día del distribuidor: si no existe se crea con los comercios de sus pedidos pendientes;
     * si existe, se agregan los comercios que todavía no estén (pedidos asignados después). Un comercio que ya
     * tuvo un intento fallido HOY no se vuelve a agregar hoy: queda para la ruta del día siguiente.
     */
    @Transactional(rollbackFor = Exception.class)
    public Optional<Ruta> asegurarRutaEntrega(Distribuidor distribuidor, LocalDate fecha,
                                              List<PuntoVenta> comercios) throws MiException {
        Optional<Ruta> existente = buscarRutaDelDia(distribuidor, TipoRuta.ENTREGA, fecha);
        if (existente.isEmpty()) {
            if (comercios.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(altaEntidad(new RutaRequestDTO(fecha, TipoRuta.ENTREGA, distribuidor.getId(),
                    comercios.stream().map(PuntoVenta::getId).distinct().toList())));
        }
        Ruta ruta = existente.get();
        boolean cambio = false;
        for (PuntoVenta comercio : comercios) {
            if (buscarParada(ruta, comercio.getId()).isEmpty()) {
                ruta.getPuntos().add(new PuntoRuta(comercio, siguienteOrden(ruta)));
                ruta.setCriterioOrden(null);
                cambio = true;
            }
        }
        return Optional.of(cambio ? repositorioRuta.save(ruta) : ruta);
    }

    /** Indica si el punto está en la ruta y todavía pendiente (para que los pedidos actualicen la visita). */
    public Optional<PuntoRuta> buscarParada(Ruta ruta, String idPuntoVenta) {
        return ruta.getPuntos().stream()
                .filter(p -> p.getPuntoVenta().getId().equals(idPuntoVenta))
                .findFirst();
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private RutaDelDiaDTO armarRutaDelDia(Ruta ruta, UbicacionDTO ubicacion,
                                          Function<PuntoVenta, List<PedidoResumenDTO>> pedidosDe) {
        RutaResponseDTO dto = convertirAResponse(ruta, pedidosDe);
        List<PuntoRutaDTO> pendientes = dto.puntos().stream()
                .filter(p -> p.resultado() == ResultadoVisita.PENDIENTE)
                .toList();
        boolean conUbicacion = ubicacion != null && ubicacion.latitud() != null && ubicacion.longitud() != null;
        PuntoRutaDTO siguiente = null;
        Double distancia = null;
        if (!pendientes.isEmpty()) {
            // proponerSiguientePunto del diagrama: el primer pendiente del orden guardado (el que decidió
            // el optimizador, o el de administración si todavía no se ordenó). Con ubicación, se informa la distancia.
            siguiente = pendientes.get(0);
            if (conUbicacion) {
                distancia = CalculoDistancia.kilometros(ubicacion.latitud(), ubicacion.longitud(),
                        siguiente.latitud(), siguiente.longitud());
            }
        }
        List<double[]> puntosTrayecto = new ArrayList<>();
        if (conUbicacion) {
            puntosTrayecto.add(new double[]{ubicacion.latitud(), ubicacion.longitud()});
        }
        pendientes.forEach(p -> puntosTrayecto.add(new double[]{p.latitud(), p.longitud()}));
        TrayectoDTO trayecto = puntosTrayecto.size() < 2 ? TrayectoDTO.vacio() : servicioMapas.trazar(puntosTrayecto);
        return new RutaDelDiaDTO(dto, siguiente, distancia, trayecto);
    }

    private static double coordenada(PuntoRuta parada, boolean latitud) {
        Direccion d = parada.getPuntoVenta().getDireccion();
        return latitud ? d.getLatitud() : d.getLongitud();
    }

    private Empleado preventista(String correo) throws MiException {
        Empleado empleado = servicioEmpleado.obtenerEmpleadoPorCorreo(correo);
        if (!(empleado instanceof Preventista)) {
            throw new MiException("La cuenta no pertenece a un preventista");
        }
        return empleado;
    }

    private static int siguienteOrden(Ruta ruta) {
        return ruta.getPuntos().stream().mapToInt(PuntoRuta::getOrden).max().orElse(0) + 1;
    }

    private static void validarUbicacionOpcional(UbicacionDTO ubicacion) throws MiException {
        if (ubicacion == null || (ubicacion.latitud() == null && ubicacion.longitud() == null)) {
            return;
        }
        if (ubicacion.latitud() == null || ubicacion.longitud() == null
                || ubicacion.latitud() < -90 || ubicacion.latitud() > 90
                || ubicacion.longitud() < -180 || ubicacion.longitud() > 180) {
            throw new MiException("La ubicación indicada no es válida");
        }
    }

    private PuntoRutaDTO aDTO(PuntoRuta parada, List<PedidoResumenDTO> pedidos) {
        PuntoVenta pv = parada.getPuntoVenta();
        Direccion d = pv.getDireccion();
        return new PuntoRutaDTO(parada.getOrden(), parada.getResultado(), parada.getMotivo(),
                parada.getFechaHoraVisita(), pv.getId(), pv.getNombreFantasia(), pv.getRazonSocial(), pv.getCuit(),
                ServicioPuntoVenta.textoDireccion(pv), d.getLatitud(), d.getLongitud(), pv.getCliente().getId(),
                pv.getCliente().getNombre() + " " + pv.getCliente().getApellido(), pedidos);
    }
}
