package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.dto.CategoriaRequestDTO;
import com.ejercicioIntegrador.dulcemayor.dto.CategoriaResponseDTO;
import com.ejercicioIntegrador.dulcemayor.exceptions.MiException;
import com.ejercicioIntegrador.dulcemayor.exceptions.RecursoNoEncontradoException;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.repository.RepositorioCategoria;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseService;
import com.ejercicioIntegrador.dulcemayor.service.base.BaseUseCaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class ServicioCategoria extends BaseService<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {

    private final RepositorioCategoria repositorioCategoria;

    public ServicioCategoria(RepositorioCategoria repositorioCategoria) {
        super(repositorioCategoria);
        this.repositorioCategoria = repositorioCategoria;
    }

    @Override
    protected Categoria crearEntidad(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(normalizarTexto(dto.nombre()));
        return categoria;
    }

    @Override
    protected void actualizarEntidad(Categoria categoria, CategoriaRequestDTO dto) {
        categoria.setNombre(normalizarTexto(dto.nombre()));
    }

    @Override
    protected void validar(BaseUseCaseService casoDeUso, Categoria categoria) throws MiException {
        if (casoDeUso == BaseUseCaseService.BAJA) {
            return;
        }
        if (estaVacio(categoria.getNombre())) {
            throw new MiException("Debe indicar el nombre de la categoría");
        }
        Optional<Categoria> existente = repositorioCategoria
                .findFirstByNombreIgnoreCaseAndEliminadoFalse(categoria.getNombre());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), categoria.getId())) {
            throw new MiException("Ya existe una categoría con el nombre " + categoria.getNombre());
        }
    }

    @Override
    public CategoriaResponseDTO convertirAResponse(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNombre(), categoria.isEliminado());
    }

    @Override
    protected String nombreEntidad() {
        return "la categoría";
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarCategoriaPorNombre(String nombre) throws MiException {
        if (estaVacio(nombre)) {
            throw new MiException("Debe indicar el nombre a buscar");
        }
        return repositorioCategoria.findFirstByNombreIgnoreCaseAndEliminadoFalse(nombre.trim())
                .map(this::convertirAResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una categoría con el nombre " + nombre));
    }
}
