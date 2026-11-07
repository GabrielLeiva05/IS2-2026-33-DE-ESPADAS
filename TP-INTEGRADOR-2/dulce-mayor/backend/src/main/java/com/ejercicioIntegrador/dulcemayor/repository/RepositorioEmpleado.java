package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.RolUsuario;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.model.Empleado;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

/** Repositorio de Empleado: por la herencia JOINED tambiÃ©n devuelve Preventistas y Distribuidores. */
public interface RepositorioEmpleado extends BaseRepository<Empleado> {

    Optional<Empleado> findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(TipoDocumento tipoDocumento,
                                                                                    String numeroDocumento);

    Optional<Empleado> findFirstByLegajoIgnoreCaseAndEliminadoFalse(String legajo);

    boolean existsByLegajoIgnoreCase(String legajo);

    Optional<Empleado> findFirstByUsuario_Id(String idUsuario);

    boolean existsByUsuario_RolAndEliminadoFalse(RolUsuario rol);

    List<Empleado> findByUsuario_RolAndEliminadoFalseOrderByApellidoAscNombreAsc(RolUsuario rol);
}
