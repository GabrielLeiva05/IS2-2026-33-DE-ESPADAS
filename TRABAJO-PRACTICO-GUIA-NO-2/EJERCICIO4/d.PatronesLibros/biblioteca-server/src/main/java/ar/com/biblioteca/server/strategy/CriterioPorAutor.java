package ar.com.biblioteca.server.strategy;

import ar.com.biblioteca.server.entities.Autor;
import ar.com.biblioteca.server.entities.Libro;
import org.springframework.stereotype.Component;

/**
 * ConcreteStrategy: algún autor del libro (nombre o apellido) contiene el texto buscado.
 * También mira el texto "autor" del libro, por si se cargó a mano.
 * GET /api/v1/libros?criterio=autor&valor=...
 */
@Component("autor")
public class CriterioPorAutor implements CriterioBusquedaLibro {

    @Override
    public boolean cumple(Libro libro, String valor) {
        for (Autor autor : libro.getAutores()) {
            if (CriterioBusquedaLibro.contiene(autor.getNombre() + " " + autor.getApellido(), valor)) {
                return true;
            }
        }
        return CriterioBusquedaLibro.contiene(libro.getAutor(), valor);
    }
}
