package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {

    /** Cantidad de domicilios que referencian a una localidad (para impedir su borrado si está en uso). */
    long countByLocalidadId(Long localidadId);
}
