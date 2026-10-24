package ar.com.biblioteca.server.iterator;

import ar.com.biblioteca.server.entities.Libro;

import java.util.ArrayList;
import java.util.List;

/**
 * ITERATOR — el "Aggregate" (la colección que se recorre).
 * <p>
 * Guarda los libros y ENTREGA iteradores para recorrerlos. Quien la usa nunca toca la lista
 * interna: pide un iterador y avanza con tieneSiguiente() / siguiente().
 */
public class ColeccionLibros {

    private final List<Libro> libros;

    public ColeccionLibros(List<Libro> libros) {
        this.libros = new ArrayList<>(libros);
    }

    /** Iterador que solo devuelve los libros escritos por ese autor. */
    public Iterador<Libro> iteradorPorAutor(Long autorId) {
        return new IteradorLibrosPorAutor(libros, autorId);
    }
}
