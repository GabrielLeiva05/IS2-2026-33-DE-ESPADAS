package com.generico.patronTemplate.business.percistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generico.patronTemplate.business.domain.entity.Empresa;

public interface EmpresaRepository extends BaseRepository<Empresa, Long> {
	
	@Query("SELECT e "
		 + "  FROM Empresa e "
		 + " WHERE e.cuit = :cuit "
		 + "   AND e.eliminado = FALSE")
	public Empresa buscarEmpresaPorCuit(@Param("cuit")String cuit);
}







