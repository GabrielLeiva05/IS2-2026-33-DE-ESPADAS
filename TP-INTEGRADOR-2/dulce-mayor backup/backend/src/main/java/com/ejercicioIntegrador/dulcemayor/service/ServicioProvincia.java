package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.ProvinciaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Provincia;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioProvincia;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Reutiliza la validación de ProvinciaServicio del Integrador 1, ahora dentro del Template Method. */
@Service
public class ServicioProvincia extends BaseService<Provincia, ProvinciaRequestDTO, ProvinciaResponseDTO> {

    private final RepositorioProvincia repositorioProvincia;
    private final ServicioPais servicioPais;

    public ServicioProvincia(RepositorioProvincia repositorioProvincia, ServicioPais servicioPais) {
        super(repositorioProvincia);
        this.repositorioProvincia = repositorioProvincia;
        this.servicioPais = servicioPais;
    }

    @Override
    protected Provincia crearEntidad(ProvinciaRequestDTO dto) throws MiException {
        Provincia provincia = new Provincia();
        actualizarEntidad(provincia, dto);
        return provincia;
    }

    @Override
    protected void actualizarEntidad(Provincia provincia, ProvinciaRequestDTO dto) throws MiException {
        provincia.setNombre(normalizarTexto(dto.nombre()));
        provincia.setPais(obtenerReferencia(servicioPais, dto.idPais(), "el país"));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Provincia provincia) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(provincia.getNombre())) {
            throw new MiException("El nombre de la provincia no puede estar vacío");
        }
        if (provincia.getPais() == null || provincia.getPais().isEliminado()) {
            throw new MiException("Debe asociar un país activo a la provincia");
        }
        Optional<Provincia> existente = repositorioProvincia.findFirstByPais_IdAndNombreIgnoreCaseAndEliminadoFalse(
                provincia.getPais().getId(), provincia.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), provincia.getId())) {
            throw new MiException("El país ya tiene una provincia llamada " + provincia.getNombre());
        }
    }

    @Override
    public ProvinciaResponseDTO convertirAResponse(Provincia provincia) {
        return new ProvinciaResponseDTO(provincia.getId(), provincia.getNombre(), provincia.getPais().getId(),
                provincia.getPais().getNombre(), provincia.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la provincia";
    }

    @Transactional(readOnly = true)
    public ProvinciaResponseDTO buscarProvinciaPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioProvincia.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la provincia " + nombre));
    }

    @Transactional(readOnly = true)
    public List<ProvinciaResponseDTO> listarProvincia(String idPais) throws MiException {
        servicioPais.obtenerEntidad(idPais);
        return convertirAResponse(repositorioProvincia.findByPais_IdOrderByNombreAsc(idPais));
    }

    @Transactional(readOnly = true)
    public List<ProvinciaResponseDTO> listarProvinciaActivo(String idPais) throws MiException {
        servicioPais.obtenerEntidad(idPais);
        return convertirAResponse(repositorioProvincia.findByPais_IdAndEliminadoFalseOrderByNombreAsc(idPais));
    }
}
