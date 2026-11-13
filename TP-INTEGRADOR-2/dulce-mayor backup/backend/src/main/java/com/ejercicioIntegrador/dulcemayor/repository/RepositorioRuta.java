package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoRuta;
import com.ejercicioIntegrador.dulcemayor.model.Ruta;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioRuta extends BaseRepository<Ruta> {

    Optional<Ruta> findFirstByEmpleado_IdAndFechaAndTipoRutaAndEliminadoFalse(String idEmpleado, LocalDate fecha,
                                                                             TipoRuta tipoRuta);

    List<Ruta> findByFechaAndEliminadoFalse(LocalDate fecha);
}
