package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Localidad;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.exceptions.ResourceNotFoundException;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LibroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AutorService extends ServicioBase<Autor>{

    private final LibroRepository libroRepository;

    public AutorService(AutorRepository repository, LibroRepository libroRepository) {
        super(repository);

        this.libroRepository = libroRepository;
    }


    @Override protected String nombreEntidad() { return "el autor"; }
    @Override protected Sort ordenamiento() { return Sort.by("Id"); }

    @Override
    protected void copiarDatos(Autor destino, Autor origen) {
        destino.setNombre(origen.getNombre());
        destino.setApellido(origen.getApellido());
        destino.setBiografia(origen.getBiografia());
    }

    @Override
    protected void antesDeEliminar(Autor existente) {
        long enUso = libroRepository.countByAutoresId(existente.getId());
        if (enUso > 0) {
            throw new ConflictException("No se puede eliminar la localidad \"" + existente.getNombre()
                    + "\" porque está siendo utilizada por " + enUso + " domicilio(s)");
        }
    }
}


