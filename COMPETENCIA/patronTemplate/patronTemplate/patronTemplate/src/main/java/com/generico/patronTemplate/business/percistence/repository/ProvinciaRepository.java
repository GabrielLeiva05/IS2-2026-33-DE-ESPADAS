package com.generico.patronTemplate.business.percistence.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generico.patronTemplate.business.domain.entity.Provincia;

public interface ProvinciaRepository extends BaseRepository<Provincia, Long> {
	
	@Query("SELECT p "
            + "  FROM Provincia p"
            + " WHERE p.eliminado = FALSE"
            + "   AND p.pais.id = :idPais")		
	public List<Provincia> listarProvinciaActiva(@Param("idPais") Long idPais);
	
	@Query("SELECT p "
		     + "  FROM Provincia p"
		     + " WHERE p.nombre = :nombre"
		     + "   AND p.eliminado = FALSE")
	public Provincia buscarProvinciaPorNombre (@Param("nombre")String nombre);
		  
	@Query("SELECT p "
	     + "  FROM Provincia p"
	     + " WHERE p.pais.id = :idPais"
	     + "   AND p.nombre = :nombre"
	     + "   AND p.eliminado = FALSE")
	public Provincia buscarProvinciaPorPaisYNombre (@Param("idPais")Long idPais, @Param("nombre")String nombre);
}







