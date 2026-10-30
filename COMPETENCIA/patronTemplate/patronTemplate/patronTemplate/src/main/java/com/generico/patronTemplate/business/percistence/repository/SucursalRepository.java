package com.generico.patronTemplate.business.percistence.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generico.patronTemplate.business.domain.entity.Sucursal;


public interface SucursalRepository extends BaseRepository<Sucursal, Long> {
	
	@Query("SELECT s "
	     + "  FROM Sucursal s "
	     + " WHERE s.nombre = :nombre "
	     + "   AND s.eliminado = FALSE")
	public Sucursal buscarSucursalPorNombre(@Param("nombre")String nombre);
	
	@Query("SELECT s "
		 + "  FROM Sucursal s "
		 + " WHERE s.empresa.id = :idEmpresa"
		 + "   AND s.eliminado = FALSE")
	public Collection<Sucursal> listarSucursalPorEmpresa(@Param("idEmpresa")Long idEmpresa);
}







