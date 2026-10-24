package ar.com.biblioteca.server.services;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;
import ar.com.biblioteca.server.exceptions.ConflictException;
import ar.com.biblioteca.server.iterator.ColeccionLibros;
import ar.com.biblioteca.server.iterator.Iterador;
import ar.com.biblioteca.server.repositories.AutorRepository;
import ar.com.biblioteca.server.repositories.LibroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutorService extends ServicioBase<Autor>{

    private final LibroRepository libroRepository;

    public AutorService(AutorRepository repository, LibroRepository libroRepository) {
        super(repository);

        this.libroRepository = libroRepository;
    }


    @Override protected String nombreEntidad() { return "el autor"; }
    @Override protected Sort ordenamiento() { return Sort.by("id"); }

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
            throw new ConflictException("No se puede eliminar el autor \"" + existente.descripcion()
                    + "\" porque figura en " + enUso + " libro(s)");
        }
    }

    // ------------------------------------------------------------------ PROTOTYPE

    /**
     * Clona un autor: el original se copia a sí mismo (clonar()) y la copia se guarda como un
     * autor NUEVO con el método plantilla create() (id nuevo, mismos datos). Después se edita.
     */
    @Transactional
    public Autor clonar(Long id) {
        Autor original = findById(id);
        Autor copia = original.clonar();   // ← PROTOTYPE
        return create(copia);
    }

    // ------------------------------------------------------------------ ITERATOR

    /**
     * Libros de un autor: se recorre la lista de TODOS los libros con un iterador que va
     * salteando los que no son de ese autor.
     */
    @Transactional(readOnly = true)
    public List<Libro> librosDelAutor(Long autorId) {
        Autor autor = findById(autorId); // 404 si el autor no existe
        ColeccionLibros coleccion = new ColeccionLibros(libroRepository.findAll(Sort.by("titulo")));

        Iterador<Libro> iterador = coleccion.iteradorPorAutor(autor.getId());
        List<Libro> resultado = new ArrayList<>();
        while (iterador.tieneSiguiente()) {
            resultado.add(iterador.siguiente());
        }
        return resultado;
    }
}


