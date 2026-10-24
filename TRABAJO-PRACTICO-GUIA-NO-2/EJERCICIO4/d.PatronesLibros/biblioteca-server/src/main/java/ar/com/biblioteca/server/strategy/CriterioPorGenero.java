package ar.com.biblioteca.server.strategy;

import ar.com.biblioteca.server.entities.Libro;
import org.springframework.stereotype.Component;

/** ConcreteStrategy: el género contiene el texto buscado. GET /api/v1/libros?criterio=genero&valor=... */
@Component("genero")
public class CriterioPorGenero implements CriterioBusquedaLibro {

    @Override
    public boolean cumple(Libro libro, String valor) {
        return CriterioBusquedaLibro.contiene(libro.getGenero(), valor);
    }
}
