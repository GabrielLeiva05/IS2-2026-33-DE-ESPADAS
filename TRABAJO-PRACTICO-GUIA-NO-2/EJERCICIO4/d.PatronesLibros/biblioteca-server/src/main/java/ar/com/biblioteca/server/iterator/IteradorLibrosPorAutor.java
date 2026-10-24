package ar.com.biblioteca.server.iterator;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * ITERATOR — el "ConcreteIterator".
 * <p>
 * Recorre la lista de libros y en cada paso SALTEA los que no son del autor indicado:
 * siguiente() siempre devuelve el próximo libro de ese autor. El que lo usa solo hace
 * <pre>
 *   while (it.tieneSiguiente()) { Libro libro = it.siguiente(); ... }
 * </pre>
 * y no sabe nada del filtro ni de la posición en la lista (eso lo guarda el iterador).
 */
public class IteradorLibrosPorAutor implements Iterador<Libro> {

    private final List<Libro> libros;
    private final Long autorId;
    private int posicion = 0;

    IteradorLibrosPorAutor(List<Libro> libros, Long autorId) {
        this.libros = libros;
        this.autorId = autorId;
    }

    @Override
    public boolean tieneSiguiente() {
        avanzarHastaElProximoDelAutor();
        return posicion < libros.size();
    }

    @Override
    public Libro siguiente() {
        if (!tieneSiguiente()) {
            throw new NoSuchElementException("No hay más libros de este autor");
        }
        return libros.get(posicion++);
    }

    /** Mueve la posición hasta el próximo libro del autor (o hasta el final). */
    private void avanzarHastaElProximoDelAutor() {
        while (posicion < libros.size() && !esDelAutor(libros.get(posicion))) {
            posicion++;
        }
    }

    private boolean esDelAutor(Libro libro) {
        for (Autor autor : libro.getAutores()) {
            if (autor.getId().equals(autorId)) {
                return true;
            }
        }
        return false;
    }
}
