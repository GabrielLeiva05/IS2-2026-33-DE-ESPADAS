package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.AsignarRutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CerrarParadaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ParadaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ReordenarRutaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.RutaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoParada;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoPedido;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.EstadoRuta;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.model.Direccion;
import com.ejercicioIntegrador.dulcemayor.model.Distribuidor;
import com.ejercicioIntegrador.dulcemayor.model.Parada;
import com.ejercicioIntegrador.dulcemayor.model.Pedido;
import com.ejercicioIntegrador.dulcemayor.model.PuntoVenta;
import com.ejercicioIntegrador.dulcemayor.model.Ruta;
import com.ejercicioIntegrador.dulcemayor.model.Usuario;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioEmpleado;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioParada;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPedido;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioRuta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Hojas de ruta y entregas (Fase 5). La asignación la hace administración; el distribuidor solo
 * ve su propia jornada. Al entregar se registra el cobro en efectivo si correspondía.
 */
@Service
public class ServicioRuta {

    private final RepositorioRuta repositorioRuta;
    private final RepositorioParada repositorioParada;
    private final RepositorioPedido repositorioPedido;
    private final RepositorioEmpleado repositorioEmpleado;
    private final ServicioUsuario servicioUsuario;
    private final ServicioEmpleado servicioEmpleado;
    private final ServicioPago servicioPago;
    private final org.springframework.context.ApplicationEventPublisher eventos;

    public ServicioRuta(RepositorioRuta repositorioRuta, RepositorioParada repositorioParada,
                        RepositorioPedido repositorioPedido, RepositorioEmpleado repositorioEmpleado,
                        ServicioUsuario servicioUsuario, ServicioEmpleado servicioEmpleado, ServicioPago servicioPago,
                        org.springframework.context.ApplicationEventPublisher eventos) {
        this.repositorioRuta = repositorioRuta;
        this.repositorioParada = repositorioParada;
        this.repositorioPedido = repositorioPedido;
        this.repositorioEmpleado = repositorioEmpleado;
        this.servicioUsuario = servicioUsuario;
        this.servicioEmpleado = servicioEmpleado;
        this.servicioPago = servicioPago;
        this.eventos = eventos;
    }

    // ------------------------------------------------------------------ administración

    @Transactional(readOnly = true)
    public List<RutaResponseDTO> listar(LocalDate fecha) {
        List<Ruta> rutas = fecha == null ? repositorioRuta.findByEliminadoFalse()
                : repositorioRuta.findByFechaAndEliminadoFalseOrderByFechaAsc(fecha);
        return rutas.stream().map(ruta -> convertir(ruta)).toList();
    }

    /** Distribuidores disponibles para asignar una jornada. */
    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO asignar(AsignarRutaRequestDTO dto) throws MiException {
        if (dto == null || dto.idsPedidos() == null || dto.idsPedidos().isEmpty()) {
            throw new MiException("Elegí al menos un pedido para la hoja de ruta");
        }
        if (dto.idDistribuidor() == null || dto.idDistribuidor().isBlank()) {
            throw new MiException("Elegí el distribuidor");
        }
        Distribuidor distribuidor = buscarDistribuidor(dto.idDistribuidor());
        LocalDate fecha = dto.fecha() == null ? LocalDate.now() : dto.fecha();

        Ruta ruta = repositorioRuta.findFirstByFechaAndDistribuidor_IdAndEliminadoFalse(fecha, distribuidor.getId())
                .orElseGet(() -> {
                    Ruta nueva = new Ruta();
                    nueva.setFecha(fecha);
                    nueva.setDistribuidor(distribuidor);
                    nueva.setEstado(EstadoRuta.PLANIFICADA);
                    return repositorioRuta.save(nueva);
                });
        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new MiException("La hoja de ruta de ese día ya está cerrada");
        }
        if (dto.observacion() != null && !dto.observacion().isBlank()) {
            ruta.setObservacion(dto.observacion().trim());
        }

        List<Parada> paradas = new ArrayList<>(ruta.getParadas());
        int orden = paradas.stream().mapToInt(Parada::getOrden).max().orElse(0);
        for (String idPedido : dto.idsPedidos()) {
            Pedido pedido = repositorioPedido.findByIdAndEliminadoFalse(idPedido)
                    .orElseThrow(() -> new MiException("No existe el pedido " + idPedido));
            if (pedido.getEstado() != EstadoPedido.CONFIRMADO && pedido.getEstado() != EstadoPedido.COMPLETADO) {
                throw new MiException("Solo se pueden asignar pedidos confirmados o pagos: " + idPedido);
            }
            if (repositorioParada.existsByPedido_IdAndEstadoNotAndEliminadoFalse(idPedido, EstadoParada.FALLIDO)
                    && paradas.stream().noneMatch(parada -> parada.getPedido().getId().equals(idPedido))) {
                throw new MiException("El pedido ya está asignado a otra hoja de ruta: " + idPedido);
            }
            if (paradas.stream().anyMatch(parada -> parada.getPedido().getId().equals(idPedido))) {
                throw new MiException("El pedido ya está en esta hoja de ruta: " + idPedido);
            }
            Parada parada = new Parada();
            parada.setRuta(ruta);
            parada.setPedido(pedido);
            parada.setOrden(++orden);
            paradas.add(parada);
        }
        ruta.getParadas().clear();
        ruta.getParadas().addAll(paradas);
        return convertir(repositorioRuta.save(ruta));
    }

    @Transactional(readOnly = true)
    public RutaResponseDTO detalle(String idRuta) throws MiException {
        return convertir(buscarRuta(idRuta));
    }

    @Transactional(readOnly = true)
    public RutaResponseDTO detalleDelDistribuidor(String idRuta, String correoDistribuidor) throws MiException {
        return convertir(rutaDelDistribuidor(idRuta, correoDistribuidor));
    }

    // ------------------------------------------------------------------ distribuidor

    @Transactional(readOnly = true)
    public Optional<RutaResponseDTO> miRuta(String correoDistribuidor) throws MiException {
        Distribuidor distribuidor = distribuidorDeCuenta(correoDistribuidor);
        return repositorioRuta.findFirstByFechaAndDistribuidor_IdAndEliminadoFalse(LocalDate.now(), distribuidor.getId())
                .or(() -> repositorioRuta.findFirstByDistribuidor_IdAndEstadoNotAndEliminadoFalseOrderByFechaDesc(
                        distribuidor.getId(), EstadoRuta.CERRADA))
                .map(ruta -> convertir(ruta));
    }

    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO reordenar(String idRuta, ReordenarRutaRequestDTO dto, String correoDistribuidor)
            throws MiException {
        Ruta ruta = rutaDelDistribuidor(idRuta, correoDistribuidor);
        if (dto == null || dto.idsParadas() == null || dto.idsParadas().isEmpty()) {
            throw new MiException("Indicá el nuevo orden de las paradas");
        }
        List<Parada> paradas = repositorioParada.findByRuta_IdAndEliminadoFalseOrderByOrdenAsc(ruta.getId());
        if (dto.idsParadas().size() != paradas.size()) {
            throw new MiException("El nuevo orden debe incluir todas las paradas de la ruta");
        }
        int orden = 1;
        for (String idParada : dto.idsParadas()) {
            Parada parada = paradas.stream().filter(p -> p.getId().equals(idParada)).findFirst()
                    .orElseThrow(() -> new MiException("La parada no pertenece a esta ruta"));
            parada.setOrden(orden++);
            repositorioParada.save(parada);
        }
        return convertir(buscarRuta(idRuta));
    }

    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO entregar(String idParada, CerrarParadaRequestDTO dto, String correoDistribuidor)
            throws MiException {
        Parada parada = paradaDelDistribuidor(idParada, correoDistribuidor);
        if (parada.getEstado() != EstadoParada.PENDIENTE) {
            throw new MiException("La parada ya fue resuelta");
        }
        Pedido pedido = parada.getPedido();
        boolean cobroEfectivo = false;
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
            servicioPago.cobrarEnEntrega(pedido.getId());
            cobroEfectivo = true;
        }
        pedido.setEstado(EstadoPedido.ENTREGADO);
        parada.setEstado(EstadoParada.ENTREGADO);
        parada.setCobroEfectivo(cobroEfectivo);
        parada.setObservacion(dto == null ? null : recortar(dto.observacion()));
        parada.setFechaResolucion(LocalDateTime.now());
        repositorioParada.save(parada);
        notificar(pedido, com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo.ENTREGA_REGISTRADA);
        actualizarEstadoRuta(parada.getRuta());
        return convertir(buscarRuta(parada.getRuta().getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public RutaResponseDTO fallar(String idParada, CerrarParadaRequestDTO dto, String correoDistribuidor)
            throws MiException {
        Parada parada = paradaDelDistribuidor(idParada, correoDistribuidor);
        if (parada.getEstado() != EstadoParada.PENDIENTE) {
            throw new MiException("La parada ya fue resuelta");
        }
        if (dto == null || dto.motivo() == null || dto.motivo().isBlank()) {
            throw new MiException("Indicá el motivo del intento fallido");
        }
        parada.setEstado(EstadoParada.FALLIDO);
        parada.setMotivoFallo(recortar(dto.motivo()));
        parada.setObservacion(dto.observacion() == null ? null : recortar(dto.observacion()));
        parada.setFechaResolucion(LocalDateTime.now());
        repositorioParada.save(parada);
        actualizarEstadoRuta(parada.getRuta());
        return convertir(buscarRuta(parada.getRuta().getId()));
    }

    // ------------------------------------------------------------------ auxiliares

    private Distribuidor buscarDistribuidor(String id) throws MiException {
        return repositorioEmpleado.findByIdAndEliminadoFalse(id)
                .filter(Distribuidor.class::isInstance)
                .map(Distribuidor.class::cast)
                .orElseThrow(() -> new MiException("El empleado indicado no es distribuidor"));
    }

    private Distribuidor distribuidorDeCuenta(String correo) throws MiException {
        Usuario usuario = servicioUsuario.buscarActivoPorNombreUsuario(correo)
                .orElseThrow(() -> new MiException("No se encontró la cuenta autenticada"));
        if (usuario.getRol() != RolUsuario.DISTRIBUIDOR) {
            throw new MiException("La cuenta autenticada no tiene perfil de distribuidor");
        }
        return servicioEmpleado.buscarPorUsuario(usuario.getId())
                .filter(Distribuidor.class::isInstance)
                .map(Distribuidor.class::cast)
                .orElseThrow(() -> new MiException("No se encontró el distribuidor asociado a la cuenta"));
    }

    private Ruta buscarRuta(String idRuta) throws MiException {
        return repositorioRuta.findByIdAndEliminadoFalse(idRuta)
                .orElseThrow(() -> new MiException("No existe la hoja de ruta indicada"));
    }

    private Ruta rutaDelDistribuidor(String idRuta, String correo) throws MiException {
        Distribuidor distribuidor = distribuidorDeCuenta(correo);
        Ruta ruta = buscarRuta(idRuta);
        if (!Objects.equals(ruta.getDistribuidor().getId(), distribuidor.getId())) {
            throw new MiException("La hoja de ruta no está asignada a tu usuario");
        }
        return ruta;
    }

    private Parada paradaDelDistribuidor(String idParada, String correo) throws MiException {
        Parada parada = repositorioParada.findByIdAndEliminadoFalse(idParada)
                .orElseThrow(() -> new MiException("No existe la parada indicada"));
        rutaDelDistribuidor(parada.getRuta().getId(), correo);
        return parada;
    }

    private void actualizarEstadoRuta(Ruta ruta) {
        List<Parada> paradas = repositorioParada.findByRuta_IdAndEliminadoFalseOrderByOrdenAsc(ruta.getId());
        boolean algunaResuelta = paradas.stream().anyMatch(p -> p.getEstado() != EstadoParada.PENDIENTE);
        boolean todasResueltas = !paradas.isEmpty() && paradas.stream().allMatch(p -> p.getEstado() != EstadoParada.PENDIENTE);
        if (todasResueltas) {
            ruta.setEstado(EstadoRuta.CERRADA);
        } else if (algunaResuelta) {
            ruta.setEstado(EstadoRuta.EN_CURSO);
        }
        repositorioRuta.save(ruta);
    }

    private RutaResponseDTO convertir(Ruta ruta) {
        List<Parada> paradas = ruta.getParadas().stream().sorted(Comparator.comparingInt(Parada::getOrden)).toList();
        List<ParadaResponseDTO> paradasDto = paradas.stream().map(this::convertirParada).toList();
        String siguiente = siguienteParadaId(paradas);
        BigDecimal totalCobrado = paradas.stream()
                .filter(parada -> parada.getEstado() == EstadoParada.ENTREGADO && parada.isCobroEfectivo())
                .map(parada -> parada.getPedido().getTotal() == null ? BigDecimal.ZERO : parada.getPedido().getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendientes = paradas.stream().filter(parada -> parada.getEstado() == EstadoParada.PENDIENTE).count();
        return new RutaResponseDTO(ruta.getId(), ruta.getFecha(), ruta.getDistribuidor().getId(),
                nombreDistribuidor(ruta.getDistribuidor()), ruta.getDistribuidor().getVehiculo(),
                ruta.getDistribuidor().getPatente(), ruta.getEstado(), ruta.getObservacion(),
                paradasDto, siguiente, totalCobrado, pendientes);
    }

    private ParadaResponseDTO convertirParada(Parada parada) {
        Pedido pedido = parada.getPedido();
        PuntoVenta punto = pedido.getPuntoVenta();
        Direccion direccion = punto.getDireccion();
        return new ParadaResponseDTO(parada.getId(), pedido.getId(), punto.getNombreComercial(),
                direccion.getCalle(), direccion.getNumeracion(), direccion.getBarrio(),
                direccion.getLocalidad() == null ? null : direccion.getLocalidad().getNombre(),
                direccion.getLatitud(), direccion.getLongitud(), parada.getOrden(), parada.getEstado(),
                parada.getMotivoFallo(), parada.getObservacion(), parada.getFechaResolucion(),
                parada.isCobroEfectivo(), pedido.getTotal(), pedido.getEstado() == null ? null : pedido.getEstado().name());
    }

    /** Sugerencia por distancia en línea recta desde la última parada resuelta (o desde la primera). */
    private String siguienteParadaId(List<Parada> paradas) {
        List<Parada> pendientes = paradas.stream().filter(p -> p.getEstado() == EstadoParada.PENDIENTE).toList();
        if (pendientes.isEmpty()) {
            return null;
        }
        Optional<Parada> referencia = paradas.stream()
                .filter(p -> p.getEstado() != EstadoParada.PENDIENTE && coordenadas(p).isPresent())
                .reduce((primera, segunda) -> segunda);
        if (referencia.isEmpty()) {
            return pendientes.get(0).getId();
        }
        double[] origen = coordenadas(referencia.get()).orElseThrow();
        return pendientes.stream()
                .filter(p -> coordenadas(p).isPresent())
                .min(Comparator.comparingDouble(p -> distancia(origen, coordenadas(p).orElseThrow())))
                .orElse(pendientes.get(0)).getId();
    }

    private static Optional<double[]> coordenadas(Parada parada) {
        Direccion direccion = parada.getPedido().getPuntoVenta().getDireccion();
        if (direccion.getLatitud() == null || direccion.getLongitud() == null) {
            return Optional.empty();
        }
        return Optional.of(new double[]{direccion.getLatitud(), direccion.getLongitud()});
    }

    private static double distancia(double[] a, double[] b) {
        double dLat = a[0] - b[0];
        double dLon = a[1] - b[1];
        return dLat * dLat + dLon * dLon;
    }

    private static String nombreDistribuidor(Distribuidor distribuidor) {
        return (distribuidor.getNombre() + " " + distribuidor.getApellido()).trim();
    }

    private static String recortar(String valor) {
        String limpio = valor == null ? null : valor.trim();
        return limpio == null || limpio.isEmpty() ? null : limpio;
    }

    /** Avisa al comercio sobre el avance de su entrega (Observer de eventos del pedido). */
    private void notificar(Pedido pedido, com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoCorreo tipo) {
        String correo = pedido.getPuntoVenta() == null ? null : pedido.getPuntoVenta().getEmailContacto();
        String comercio = pedido.getPuntoVenta() == null ? null : pedido.getPuntoVenta().getNombreComercial();
        eventos.publishEvent(new com.ejercicioIntegrador.dulcemayor.eventos.EventoPedido(pedido.getId(), tipo, correo,
                comercio, null));
    }
}