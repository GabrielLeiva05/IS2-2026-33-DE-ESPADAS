package com.generico.patronTemplate.business.percistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.generico.patronTemplate.business.domain.entity.Localidad;

public interface LocalidadRepository extends BaseRepository<Localidad, Long> {
	
	@Query("SELECT p "
	         + "  FROM Localidad p"
	         + " WHERE p.nombre = :nombre"
	         + "   AND p.eliminado = FALSE")
    public Localidad buscarLocalidadPorNombre (@Param("nombre")String nombre);
	
	 @Query("SELECT p "
        + "  FROM Localidad p"
        + " WHERE p.departamento.id = :idDepartamento"
        + "   AND p.nombre = :nombre"
        + "   AND p.eliminado = FALSE")
	 public Localidad buscarLocalidadPorDepartamentoYNombre (@Param("idDepartamento")Long idDepartamento, @Param("nombre")String nombre);
	 
	 @Query("SELECT p "
         + "  FROM Localidad p"
         + " WHERE p.eliminado = FALSE"
         + "   AND p.departamento.id = :idDepartamento")		
	 public List<Localidad> listarLocalidadActiva(@Param("idDepartamento") Long idDepartamento);
}







