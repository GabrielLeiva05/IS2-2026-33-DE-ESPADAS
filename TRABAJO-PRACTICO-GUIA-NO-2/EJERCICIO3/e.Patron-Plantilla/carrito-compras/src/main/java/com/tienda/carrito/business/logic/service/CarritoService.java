package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Carrito;
import com.tienda.carrito.business.domain.entity.Detalle;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Reglas del Carrito: pertenece a un usuario existente; un solo carrito activo por usuario; el total se
 * recalcula siempre en el servidor; solo se quitan detalles del propio carrito; no se confirma un carrito vacio;
 * confirmar la compra CIERRA el carrito (baja logica).
 *
 * Todas las operaciones reciben el id del usuario DUENIO del carrito: un usuario nunca puede tocar el carrito de otro.
 * Solo conoce CarritoRepository; usa UsuarioService y DetalleService para el resto.
 */
@Service
public class CarritoService extends BaseService<Carrito, String> {

    private final CarritoRepository carritoRepository;
    private final UsuarioService usuarioService;
    private final DetalleService detalleService;

    public CarritoService(CarritoRepository carritoRepository,
                          UsuarioService usuarioService,
                          DetalleService detalleService) {
        super(carritoRepository);
        this.carritoRepository = carritoRepository;
        this.usuarioService = usuarioService;
        this.detalleService = detalleService;
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Carrito carrito) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return;
        }
        if (carrito.getUsuario() == null) {
            throw new ErrorServiceException("El carrito debe pertenecer a un usuario.");
        }
        if (carrito.getTotal() < 0) {
            throw new ErrorServiceException("El total del carrito no puede ser negativo.");
        }
    }

    @Override
    protected void preAlta(Carrito carrito) {
        carrito.setTotal(0);
        carrito.setDetalles(new ArrayList<>());
    }

    // ===================== Casos de uso del carrito =====================

    /** Carrito activo del usuario, si lo tiene (no lo crea). */
    @Transactional(readOnly = true)
    public Optional<Carrito> buscarActivo(String usuarioId) {
        return Optional.ofNullable(carritoRepository.buscarCarritoActivoPorUsuario(usuarioId));
    }

    /** Devuelve el carrito activo del usuario; si no tiene, crea uno nuevo. */
    @Transactional(rollbackFor = Exception.class)
    public Carrito obtenerCarritoActivo(String usuarioId) throws ErrorServiceException {
        Carrito activo = carritoRepository.buscarCarritoActivoPorUsuario(usuarioId);
        if (activo != null) {
            return activo;
        }
        Carrito nuevo = new Carrito();
        nuevo.setUsuario(usuarioService.obtenerEntidad(usuarioId));
        return alta(nuevo);
    }

    /** Agrega una unidad del articulo al carrito del usuario y recalcula el total. */
    @Transactional(rollbackFor = Exception.class)
    public Carrito agregarArticulo(String usuarioId, String articuloId) throws ErrorServiceException {
        Carrito carrito = obtenerCarritoActivo(usuarioId);
        Detalle detalle = detalleService.altaParaCarrito(carrito, articuloId);
        carrito.getDetalles().add(detalle);
        recalcularTotal(carrito);
        return carritoRepository.save(carrito);
    }

    /** Quita un detalle del carrito del usuario (baja logica del detalle) y recalcula el total. */
    @Transactional(rollbackFor = Exception.class)
    public Carrito quitarDetalle(String usuarioId, String detalleId) throws ErrorServiceException {
        Carrito carrito = carritoActivoObligatorio(usuarioId);
        Detalle detalle = carrito.getDetallesActivos().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ErrorServiceException("Ese artículo no está en tu carrito."));
        detalleService.baja(detalle.getId());
        recalcularTotal(carrito);
        return carritoRepository.save(carrito);
    }

    /** Quita todos los detalles del carrito. */
    @Transactional(rollbackFor = Exception.class)
    public Carrito vaciar(String usuarioId) throws ErrorServiceException {
        Carrito carrito = carritoActivoObligatorio(usuarioId);
        for (Detalle detalle : carrito.getDetallesActivos()) {
            detalleService.baja(detalle.getId());
        }
        recalcularTotal(carrito);
        return carritoRepository.save(carrito);
    }

    /** Confirma la compra: congela el total y cierra el carrito. Devuelve el carrito con el total final. */
    @Transactional(rollbackFor = Exception.class)
    public Carrito confirmarCompra(String usuarioId) throws ErrorServiceException {
        Carrito carrito = carritoActivoObligatorio(usuarioId);
        if (carrito.getDetallesActivos().isEmpty()) {
            throw new ErrorServiceException("Tu carrito está vacío. Agregá artículos antes de confirmar.");
        }
        recalcularTotal(carrito);
        carritoRepository.save(carrito);
        baja(carrito.getId()); // plantilla de baja logica: el carrito queda cerrado
        return carrito;
    }

    /** Cantidad de articulos del carrito activo (para el numerito del menu). 0 si no tiene carrito. */
    @Transactional(readOnly = true)
    public int contarArticulos(String usuarioId) {
        Carrito activo = carritoRepository.buscarCarritoActivoPorUsuario(usuarioId);
        return activo == null ? 0 : activo.getDetallesActivos().size();
    }

    // ===================== Auxiliares privados =====================

    private Carrito carritoActivoObligatorio(String usuarioId) throws ErrorServiceException {
        Carrito activo = carritoRepository.buscarCarritoActivoPorUsuario(usuarioId);
        if (activo == null) {
            throw new ErrorServiceException("Todavía no tenés un carrito. Agregá un artículo primero.");
        }
        return activo;
    }

    /** total = suma de los precios de los articulos de los detalles activos (redondeado a 2 decimales). */
    private void recalcularTotal(Carrito carrito) {
        double suma = carrito.getDetallesActivos().stream()
                .mapToDouble(d -> d.getArticulo().getPrecio())
                .sum();
        carrito.setTotal(Math.round(suma * 100.0) / 100.0);
    }
}
