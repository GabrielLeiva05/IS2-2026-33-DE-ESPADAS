package com.is2.tinder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.is2.tinder.entities.Voto;

public interface VotoRepository extends JpaRepository<Voto, String> {
    @Query("SELECT new com.is2.tinder.dtos.ReporteVotosDTO(v.mascota2.usuario.nombre, v.mascota2.usuario.apellido, v.mascota2.nombre, COUNT(v)) "
            + "FROM Voto v GROUP BY v.mascota2.usuario.nombre, v.mascota2.usuario.apellido, v.mascota2.nombre "
            + "ORDER BY COUNT(v) DESC")
    List<com.is2.tinder.dtos.ReporteVotosDTO> reporteVotos();

    @Query("SELECT c FROM Voto c WHERE c.mascota1.id = :id ORDER BY c.fecha DESC")
    public List<Voto> buscarVotosPropios(@Param("id") String id);

    @Query("SELECT c FROM Voto c WHERE c.mascota2.id = :id ORDER BY c.fecha DESC")
    public List<Voto> buscarVotosRecibidos(@Param("id") String id);
}
