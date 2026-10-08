package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
}
