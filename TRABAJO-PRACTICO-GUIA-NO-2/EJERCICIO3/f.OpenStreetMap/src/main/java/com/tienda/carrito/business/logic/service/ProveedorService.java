package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de Proveedor: nombre obligatorio (maximo 100) y unico entre los proveedores activos.
 * Ubicacion (OpenStreetMap): direccion opcional (maximo 255); latitud y longitud van juntas (o ninguna)
 * y deben estar en rango (latitud -90..90, longitud -180..180).
 * Este servicio solo conoce ProveedorRepository.
 */
@Service
public class ProveedorService extends BaseService<Proveedor, String> {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        super(proveedorRepository);
        this.proveedorRepository = proveedorRepository;
    }

    /** Proveedores activos que tienen latitud y longitud cargadas (los que se pueden dibujar en el mapa). */
    @Transactional(readOnly = true)
    public List<Proveedor> listarConUbicacion() throws ErrorServiceException {
        return listarActivos().stream()
                .filter(p -> p.getLatitud() != null && p.getLongitud() != null)
                .toList();
    }

    @Override
    protected void validar(BaseUseCaseService useCase, Proveedor proveedor) throws ErrorServiceException {
        if (useCase == BaseUseCaseService.BAJA) {
            return;
        }
        if (vacio(proveedor.getNombre())) {
            throw new ErrorServiceException("El nombre del proveedor es obligatorio.");
        }
        String nombre = proveedor.getNombre().trim();
        if (nombre.length() > 100) {
            throw new ErrorServiceException("El nombre del proveedor no puede superar los 100 caracteres.");
        }
        Proveedor existente = proveedorRepository.buscarProveedorPorNombre(nombre);
        if (existente != null && !existente.getId().equals(proveedor.getId())) {
            throw new ErrorServiceException("Ya existe un proveedor con el nombre \"" + nombre + "\".");
        }
        validarUbicacion(proveedor);
    }

    private void validarUbicacion(Proveedor proveedor) throws ErrorServiceException {
        if (proveedor.getDireccion() != null && proveedor.getDireccion().trim().length() > 255) {
            throw new ErrorServiceException("La dirección del proveedor no puede superar los 255 caracteres.");
        }
        Double latitud = proveedor.getLatitud();
        Double longitud = proveedor.getLongitud();
        if ((latitud == null) != (longitud == null)) {
            throw new ErrorServiceException("La ubicación necesita latitud y longitud (o ninguna de las dos).");
        }
        if (latitud != null) {
            if (!(latitud >= -90 && latitud <= 90)) {
                throw new ErrorServiceException("La latitud debe estar entre -90 y 90.");
            }
            if (!(longitud >= -180 && longitud <= 180)) {
                throw new ErrorServiceException("La longitud debe estar entre -180 y 180.");
            }
        }
    }

    @Override
    protected void preAlta(Proveedor proveedor) {
        normalizar(proveedor);
    }

    @Override
    protected void preModificacion(Proveedor proveedorNuevo) {
        normalizar(proveedorNuevo);
    }

    private void normalizar(Proveedor proveedor) {
        proveedor.setNombre(proveedor.getNombre().trim());
        proveedor.setDireccion(vacio(proveedor.getDireccion()) ? null : proveedor.getDireccion().trim());
    }
}
