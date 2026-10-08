package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    /** Cantidad de libros que referencian a un autor (para impedir su borrado si está en uso). */
    long countByAutoresId(Long autorId);
}
