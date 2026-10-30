package com.generico.patronTemplate.business.percistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generico.patronTemplate.business.domain.entity.Pais;

public interface PaisRepository extends BaseRepository<Pais, Long> {
	
	@Query("SELECT p "
		 + "  FROM Pais p "
		 + " WHERE p.nombre = :nombre "
		 + "   AND p.eliminado = FALSE")
	public Pais buscarPaisPorNombre(@Param("nombre")String nombre);
}







