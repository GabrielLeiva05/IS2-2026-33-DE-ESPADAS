package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Articulo;
import com.tienda.carrito.business.domain.entity.Carrito;
import com.tienda.carrito.business.domain.entity.Detalle;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.DetalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de Detalle: pertenece a un carrito activo y referencia un articulo disponible.
 * Solo conoce DetalleRepository; para el articulo usa ArticuloService.
 */
@Service
public class DetalleService extends BaseService<Detalle, String> {

    private final ArticuloService articuloService;

    public DetalleService(DetalleRepository detalleRepository, ArticuloService articuloService) {
        super(detalleRepository);
        this.articuloService = articuloService;
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Detalle detalle) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return;
        }
        if (detalle.getCarrito() == null || Boolean.TRUE.equals(detalle.getCarrito().isEliminado())) {
            throw new ErrorServiceException("El detalle debe pertenecer a un carrito activo.");
        }
        if (detalle.getArticulo() == null || Boolean.TRUE.equals(detalle.getArticulo().isEliminado())) {
            throw new ErrorServiceException("El detalle debe referenciar un artículo disponible.");
        }
    }

    /** Crea una linea (una unidad) del articulo indicado dentro del carrito. */
    @Transactional(rollbackFor = Exception.class)
    public Detalle altaParaCarrito(Carrito carrito, String articuloId) throws ErrorServiceException {
        Articulo articulo = articuloService.obtenerEntidad(articuloId);
        Detalle detalle = new Detalle();
        detalle.setCarrito(carrito);
        detalle.setArticulo(articulo);
        return alta(detalle);
    }
}
