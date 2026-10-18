package ar.com.biblioteca.server.strategy;

import ar.com.biblioteca.server.entities.Libro;
import org.springframework.stereotype.Component;

/** ConcreteStrategy: el título contiene el texto buscado. GET /api/v1/libros?criterio=titulo&valor=... */
@Component("titulo")
public class CriterioPorTitulo implements CriterioBusquedaLibro {

    @Override
    public boolean cumple(Libro libro, String valor) {
        return CriterioBusquedaLibro.contiene(libro.getTitulo(), valor);
    }
}
