package com.tienda.carrito.business.logic.service;

import com.tienda.carrito.business.domain.entity.Proveedor;
import com.tienda.carrito.business.logic.error.ErrorServiceException;
import com.tienda.carrito.business.percistence.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

/**
 * Reglas de Proveedor: nombre obligatorio (maximo 100) y unico entre los proveedores activos.
 * Este servicio solo conoce ProveedorRepository.
 */
@Service
public class ProveedorService extends BaseService<Proveedor, String> {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        super(proveedorRepository);
        this.proveedorRepository = proveedorRepository;
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
    }

    @Override
    protected void preAlta(Proveedor proveedor) {
        proveedor.setNombre(proveedor.getNombre().trim());
    }

    @Override
    protected void preModificacion(Proveedor proveedorNuevo) {
        proveedorNuevo.setNombre(proveedorNuevo.getNombre().trim());
    }
}
