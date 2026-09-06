package com.is2.tinder.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.is2.tinder.entities.Mascota;
import com.is2.tinder.enumerations.Tipo;

public interface MascotaRepository extends JpaRepository<Mascota, String> {
    @Query("SELECT c FROM Mascota c WHERE c.usuario.id = :id")
    public List<Mascota> buscarMascotasPorUsuario(@Param("id") String id);

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id = :id AND m.baja IS NULL")
    List<Mascota> listarMascotasPorUsuario(@Param("id") String id);

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id = :id AND m.baja IS NOT NULL")
    List<Mascota> listarMascotasDeBaja(@Param("id") String id);

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id <> :id AND m.baja IS NULL")
    List<Mascota> listarMascotasMenosUsuario(@Param("id") String id);

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id <> :id AND m.baja IS NULL AND m.tipo = :tipo")
    List<Mascota> listarMascotasMenosUsuarioPorTipo(@Param("id") String id, @Param("tipo") Tipo tipo);
}
