package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.LocalidadRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.LocalidadResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Localidad;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioLocalidad;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/** Reutiliza la validación de LocalidadServicio del Integrador 1, ahora dentro del Template Method. */
@Service
public class ServicioLocalidad extends BaseService<Localidad, LocalidadRequestDTO, LocalidadResponseDTO> {

    /** Código postal argentino: 4 dígitos (5500) o CPA (M5500ABC). */
    private static final Pattern CODIGO_POSTAL = Pattern.compile("^([A-Z]\\d{4}[A-Z]{3}|\\d{4})$");

    private final RepositorioLocalidad repositorioLocalidad;
    private final ServicioDepartamento servicioDepartamento;

    public ServicioLocalidad(RepositorioLocalidad repositorioLocalidad, ServicioDepartamento servicioDepartamento) {
        super(repositorioLocalidad);
        this.repositorioLocalidad = repositorioLocalidad;
        this.servicioDepartamento = servicioDepartamento;
    }

    @Override
    protected Localidad crearEntidad(LocalidadRequestDTO dto) throws MiException {
        Localidad localidad = new Localidad();
        actualizarEntidad(localidad, dto);
        return localidad;
    }

    @Override
    protected void actualizarEntidad(Localidad localidad, LocalidadRequestDTO dto) throws MiException {
        localidad.setNombre(normalizarTexto(dto.nombre()));
        String codigoPostal = normalizarTexto(dto.codigoPostal());
        localidad.setCodigoPostal(estaVacio(codigoPostal) ? null : codigoPostal.toUpperCase());
        localidad.setDepartamento(obtenerReferencia(servicioDepartamento, dto.idDepartamento(), "el departamento"));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Localidad localidad) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(localidad.getNombre())) {
            throw new MiException("El nombre de la localidad no puede estar vacío");
        }
        if (localidad.getDepartamento() == null || localidad.getDepartamento().isEliminado()) {
            throw new MiException("Debe asociar un departamento activo a la localidad");
        }
        if (localidad.getCodigoPostal() != null && !CODIGO_POSTAL.matcher(localidad.getCodigoPostal()).matches()) {
            throw new MiException("El código postal debe tener 4 dígitos (5500) o formato CPA (M5500ABC)");
        }
        Optional<Localidad> existente = repositorioLocalidad.findFirstByDepartamento_IdAndNombreIgnoreCaseAndEliminadoFalse(
                localidad.getDepartamento().getId(), localidad.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), localidad.getId())) {
            throw new MiException("El departamento ya tiene una localidad llamada " + localidad.getNombre());
        }
    }

    @Override
    public LocalidadResponseDTO convertirAResponse(Localidad localidad) {
        return new LocalidadResponseDTO(localidad.getId(), localidad.getNombre(), localidad.getCodigoPostal(),
                localidad.getDepartamento().getId(), localidad.getDepartamento().getNombre(), localidad.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la localidad";
    }

    @Transactional(readOnly = true)
    public LocalidadResponseDTO buscarLocalidadPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioLocalidad.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la localidad " + nombre));
    }

    @Transactional(readOnly = true)
    public LocalidadResponseDTO buscarLocalidadPorCodigoPostal(String codigoPostal) throws MiException {
        if (estaVacio(codigoPostal)) {
            throw new MiException("Debe indicar el código postal a buscar");
        }
        return repositorioLocalidad.findFirstByCodigoPostalIgnoreCaseAndEliminadoFalse(codigoPostal.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una localidad con el código postal " + codigoPostal));
    }

    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> listarLocalidad(String idDepartamento) throws MiException {
        servicioDepartamento.obtenerEntidad(idDepartamento);
        return convertirAResponse(repositorioLocalidad.findByDepartamento_IdOrderByNombreAsc(idDepartamento));
    }

    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> listarLocalidadActivo(String idDepartamento) throws MiException {
        servicioDepartamento.obtenerEntidad(idDepartamento);
        return convertirAResponse(repositorioLocalidad.findByDepartamento_IdAndEliminadoFalseOrderByNombreAsc(idDepartamento));
    }
}
