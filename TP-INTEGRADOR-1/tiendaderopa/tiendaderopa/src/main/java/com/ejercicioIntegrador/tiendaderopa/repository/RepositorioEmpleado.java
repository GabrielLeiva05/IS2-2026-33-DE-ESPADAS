package com.ejercicioIntegrador.tiendaderopa.repository;

import com.ejercicioIntegrador.tiendaderopa.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioEmpleado extends JpaRepository<Empleado, String> {
    List<Empleado> findByEliminadoFalse();
    List<Empleado> findByEmpresaId(String empresaId);
}