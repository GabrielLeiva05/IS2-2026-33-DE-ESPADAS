package com.is2.tinder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.is2.tinder.entities.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, String> {
    @Query("SELECT c FROM Mascota c WHERE c.usuario.id = :id")
    public List<Mascota> buscarMascotasPorUsuario(@Param("id") String id);
}
