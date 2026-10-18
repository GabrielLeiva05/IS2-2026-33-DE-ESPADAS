package ar.com.biblioteca.server.iterator;

/**
 * Patrón de comportamiento ITERATOR — la interfaz "Iterator".
 * <p>
 * Define cómo se recorre una colección sin mostrar cómo está guardada por dentro (lista, arreglo,
 * resultado de la base...). Es la misma idea que java.util.Iterator (hasNext / next); se escribe
 * acá para que se vean todas las piezas del patrón.
 */
public interface Iterador<T> {

    boolean tieneSiguiente();

    T siguiente();
}
