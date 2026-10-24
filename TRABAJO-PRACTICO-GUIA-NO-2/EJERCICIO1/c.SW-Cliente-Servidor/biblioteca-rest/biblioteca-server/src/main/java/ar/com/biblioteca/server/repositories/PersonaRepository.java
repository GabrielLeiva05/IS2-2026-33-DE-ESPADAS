package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Persona;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    boolean existsByDni(int dni);

    boolean existsByDniAndIdNot(int dni, Long id);

    List<Persona> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String nombre, String apellido, Sort sort);
}
