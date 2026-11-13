package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.PaisRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.PaisResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Pais;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioPais;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/** Reutiliza la validación de PaisServicio del Integrador 1, ahora dentro del Template Method. */
@Service
public class ServicioPais extends BaseService<Pais, PaisRequestDTO, PaisResponseDTO> {

    private final RepositorioPais repositorioPais;

    public ServicioPais(RepositorioPais repositorioPais) {
        super(repositorioPais);
        this.repositorioPais = repositorioPais;
    }

    @Override
    protected Pais crearEntidad(PaisRequestDTO dto) {
        Pais pais = new Pais();
        actualizarEntidad(pais, dto);
        return pais;
    }

    @Override
    protected void actualizarEntidad(Pais pais, PaisRequestDTO dto) {
        pais.setNombre(normalizarTexto(dto.nombre()));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Pais pais) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(pais.getNombre())) {
            throw new MiException("El nombre del país no puede estar vacío");
        }
        Optional<Pais> existente = repositorioPais.findFirstByNombreIgnoreCaseAndEliminadoFalse(pais.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), pais.getId())) {
            throw new MiException("Ya existe el país " + pais.getNombre());
        }
    }

    @Override
    public PaisResponseDTO convertirAResponse(Pais pais) {
        return new PaisResponseDTO(pais.getId(), pais.getNombre(), pais.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "el país";
    }

    @Transactional(readOnly = true)
    public PaisResponseDTO buscarPaisPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioPais.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el país " + nombre));
    }
}
