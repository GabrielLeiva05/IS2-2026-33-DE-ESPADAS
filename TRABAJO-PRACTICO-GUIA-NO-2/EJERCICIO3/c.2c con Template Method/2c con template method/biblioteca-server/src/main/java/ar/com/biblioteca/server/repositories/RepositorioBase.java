package ar.com.biblioteca.server.repositories;

import ar.com.biblioteca.server.entities.EntidadBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RepositorioBase<E extends EntidadBase> extends JpaRepository<E, Long> {
}