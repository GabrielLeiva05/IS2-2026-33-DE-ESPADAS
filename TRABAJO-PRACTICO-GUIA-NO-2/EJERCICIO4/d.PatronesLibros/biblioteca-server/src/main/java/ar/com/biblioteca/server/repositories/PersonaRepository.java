package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Persona;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PersonaRepository extends RepositorioBase<Persona> {

    boolean existsByDni(int dni);

    boolean existsByDniAndIdNot(int dni, Long id);

    List<Persona> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
            String nombre, String apellido, Sort sort);

    @Query("select distinct p from Persona p join p.libros l " +
            "where l.fechaDevolucion = :fecha and p.email is not null")
    List<Persona> findConLibrosQueVencenEl(@Param("fecha") LocalDate fecha);

    @Query("select p from Persona p where p.email is not null " +
            "and p.fechaNacimiento is not null " +
            "and extract(month from p.fechaNacimiento) = :mes " +
            "and extract(day from p.fechaNacimiento) = :dia")
    List<Persona> findCumpleanieros(@Param("mes") int mes, @Param("dia") int dia);
}
