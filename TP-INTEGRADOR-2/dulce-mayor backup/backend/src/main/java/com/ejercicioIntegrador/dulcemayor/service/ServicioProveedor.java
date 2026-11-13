package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ProveedorRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProveedorResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Proveedor;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioProveedor;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Proveedores (diagrama, bloque Facturación y pagos): alta, modificación, baja lógica y consultas.
 * El CUIT se valida con el mismo dígito verificador que los puntos de venta y es único entre los activos.
 * La dirección se gestiona por ServicioDireccion.
 */
@Service
public class ServicioProveedor extends BaseService<Proveedor, ProveedorRequestDTO, ProveedorResponseDTO> {

    private final RepositorioProveedor repositorioProveedor;
    private final ServicioDireccion servicioDireccion;

    public ServicioProveedor(RepositorioProveedor repositorioProveedor, ServicioDireccion servicioDireccion) {
        super(repositorioProveedor);
        this.repositorioProveedor = repositorioProveedor;
        this.servicioDireccion = servicioDireccion;
    }

    @Override
    protected Proveedor crearEntidad(ProveedorRequestDTO dto) throws MiException {
        Proveedor proveedor = new Proveedor();
        copiar(proveedor, dto);
        if (dto.direccion() == null) {
            throw new MiException("Debe indicar la dirección del proveedor");
        }
        proveedor.setDireccion(servicioDireccion.crearDireccion(dto.direccion()));
        return proveedor;
    }

    @Override
    protected void actualizarEntidad(Proveedor proveedor, ProveedorRequestDTO dto) throws MiException {
        copiar(proveedor, dto);
        if (dto.direccion() != null) {
            servicioDireccion.modificar(proveedor.getDireccion().getId(), dto.direccion());
        }
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Proveedor proveedor) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(proveedor.getRazonSocial()) || proveedor.getRazonSocial().length() > 150) {
            throw new MiException("La razón social es obligatoria (hasta 150 caracteres)");
        }
        if (!ServicioPuntoVenta.cuitValido(proveedor.getCuit())) {
            throw new MiException("El CUIT no es válido: deben ser 11 dígitos con su dígito verificador");
        }
        Optional<Proveedor> mismoCuit = repositorioProveedor.findFirstByCuitAndEliminadoFalse(proveedor.getCuit());
        if (mismoCuit.isPresent() && !Objects.equals(mismoCuit.get().getId(), proveedor.getId())) {
            throw new MiException("Ya existe un proveedor con el CUIT " + proveedor.getCuit());
        }
    }

    @Override
    public ProveedorResponseDTO convertirAResponse(Proveedor proveedor) {
        return new ProveedorResponseDTO(proveedor.getId(), proveedor.getRazonSocial(), proveedor.getCuit(),
                proveedor.getDireccion() == null ? null : servicioDireccion.convertirAResponse(proveedor.getDireccion()),
                proveedor.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el proveedor";
    }

    /** buscarProveedorPorCuit(cuit) del diagrama. */
    @Transactional(readOnly = true)
    public ProveedorResponseDTO buscarProveedorPorCuit(String cuit) throws MiException {
        String normalizado = cuit == null ? null : cuit.replaceAll("\\D", "");
        return repositorioProveedor.findFirstByCuitAndEliminadoFalse(normalizado)
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un proveedor con el CUIT " + cuit));
    }

    private static void copiar(Proveedor proveedor, ProveedorRequestDTO dto) {
        proveedor.setRazonSocial(normalizarTexto(dto.razonSocial()));
        proveedor.setCuit(dto.cuit() == null ? null : dto.cuit().replaceAll("\\D", ""));
    }
}
