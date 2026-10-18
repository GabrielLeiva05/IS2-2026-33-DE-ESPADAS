package ar.com.biblioteca.server.strategy;

import ar.com.biblioteca.server.entities.Libro;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Patrón de comportamiento STRATEGY — la interfaz "Strategy" (mismo molde que
 * CriterioBusquedaDetalle de la Clínica).
 * <p>
 * Cada forma de buscar libros (por título, por género, por autor) es una clase distinta que
 * implementa esta interfaz. El "Context" es LibroService: recibe la clave del criterio, elige la
 * estrategia y la aplica, sin un if/switch por cada criterio.
 * <p>
 * La clave de cada estrategia es el nombre del bean: @Component("titulo"), ("genero"), ("autor").
 * Spring las junta en un Map&lt;String, CriterioBusquedaLibro&gt; que se inyecta en LibroService.
 * Agregar un criterio nuevo = agregar una clase, sin tocar el service ni el controller.
 */
public interface CriterioBusquedaLibro {

    /** true si el libro cumple con el criterio para el valor buscado. */
    boolean cumple(Libro libro, String valor);

    /**
     * Compara sin importar mayúsculas ni tildes: "garcia" encuentra "García Márquez".
     * (Método static de la interfaz: lo comparten todas las estrategias.)
     */
    static boolean contiene(String texto, String buscado) {
        return texto != null && normalizar(texto).contains(normalizar(buscado));
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }
}
