package com.ejercicioIntegrador.dulcemayor.repository;

import com.ejercicioIntegrador.dulcemayor.model.EnvioCorreo;
import com.ejercicioIntegrador.dulcemayor.repository.base.BaseRepository;

import java.util.List;

public interface RepositorioEnvioCorreo extends BaseRepository<EnvioCorreo> {

    List<EnvioCorreo> findByEliminadoFalseOrderByFechaCreacionDesc();

    List<EnvioCorreo> findByDestinatarioIgnoreCaseOrderByFechaCreacionDesc(String destinatario);
}
