package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
