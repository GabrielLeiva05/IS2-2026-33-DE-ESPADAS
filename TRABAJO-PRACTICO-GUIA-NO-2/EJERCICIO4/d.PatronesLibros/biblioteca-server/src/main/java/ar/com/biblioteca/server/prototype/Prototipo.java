package ar.com.biblioteca.server.prototype;

/**
 * Patrón creacional PROTOTYPE — la interfaz "Prototype".
 * <p>
 * Un objeto que la implementa sabe crear una COPIA de sí mismo. Quien necesita la copia no tiene
 * que conocer cómo se arma el objeto ni copiar atributo por atributo: le pide al original que se clone.
 *
 * @param <T> tipo del objeto que se clona
 */
public interface Prototipo<T> {

    T clonar();
}
