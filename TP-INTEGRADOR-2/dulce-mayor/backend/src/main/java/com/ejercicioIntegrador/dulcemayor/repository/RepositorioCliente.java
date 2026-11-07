package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.dulcemayor.model.Cliente;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.Optional;

public interface RepositorioCliente extends BaseRepository<Cliente> {

    Optional<Cliente> findFirstByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(TipoDocumento tipoDocumento,
                                                                                   String numeroDocumento);

    Optional<Cliente> findFirstByUsuario_Id(String idUsuario);
}
