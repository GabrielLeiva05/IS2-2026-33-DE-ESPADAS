package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.SubCategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioSubCategoria;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ServicioSubCategoria extends BaseService<SubCategoria, SubCategoriaRequestDTO, SubCategoriaResponseDTO> {

    private final RepositorioSubCategoria repositorioSubCategoria;
    private final ServicioCategoria servicioCategoria;

    public ServicioSubCategoria(RepositorioSubCategoria repositorioSubCategoria, ServicioCategoria servicioCategoria) {
        super(repositorioSubCategoria);
        this.repositorioSubCategoria = repositorioSubCategoria;
        this.servicioCategoria = servicioCategoria;
    }

    @Override
    protected SubCategoria crearEntidad(SubCategoriaRequestDTO dto) throws MiException {
        SubCategoria subCategoria = new SubCategoria();
        actualizarEntidad(subCategoria, dto);
        return subCategoria;
    }

    @Override
    protected void actualizarEntidad(SubCategoria subCategoria, SubCategoriaRequestDTO dto) throws MiException {
        subCategoria.setNombre(normalizarTexto(dto.nombre()));
        subCategoria.setCategoria(obtenerReferencia(servicioCategoria, dto.idCategoria(), "la categoría"));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, SubCategoria subCategoria) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(subCategoria.getNombre())) {
            throw new MiException("Debe indicar el nombre de la subcategoría");
        }
        Categoria categoria = subCategoria.getCategoria();
        if (categoria == null || categoria.isEliminado()) {
            throw new MiException("La categoría de la subcategoría no existe o está dada de baja");
        }
        Optional<SubCategoria> existente = repositorioSubCategoria
                .findFirstByCategoria_IdAndNombreIgnoreCaseAndEliminadoFalse(categoria.getId(), subCategoria.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), subCategoria.getId())) {
            throw new MiException("La categoría " + categoria.getNombre()
                    + " ya tiene una subcategoría llamada " + subCategoria.getNombre());
        }
    }

    @Override
    public SubCategoriaResponseDTO convertirAResponse(SubCategoria subCategoria) {
        Categoria categoria = subCategoria.getCategoria();
        return new SubCategoriaResponseDTO(subCategoria.getId(), subCategoria.getNombre(),
                categoria.getId(), categoria.getNombre(), subCategoria.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la subcategoría";
    }

    @Transactional(readOnly = true)
    public SubCategoriaResponseDTO buscarSubCategoriaPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioSubCategoria.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una subcategoría con el nombre " + nombre));
    }

    /** listarSubCategoria(idCategoria) del diagrama: incluye las dadas de baja. */
    @Transactional(readOnly = true)
    public List<SubCategoriaResponseDTO> listarSubCategoria(String idCategoria) throws MiException {
        servicioCategoria.obtenerEntidad(idCategoria);
        return convertirAResponse(repositorioSubCategoria.findByCategoria_IdOrderByNombreAsc(idCategoria));
    }

    /** listarSubCategoriaActivo(idCategoria) del diagrama. */
    @Transactional(readOnly = true)
    public List<SubCategoriaResponseDTO> listarSubCategoriaActivo(String idCategoria) throws MiException {
        servicioCategoria.obtenerEntidad(idCategoria);
        return convertirAResponse(repositorioSubCategoria.findByCategoria_IdAndEliminadoFalseOrderByNombreAsc(idCategoria));
    }
}
