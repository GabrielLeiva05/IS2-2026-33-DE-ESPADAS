package com.example.securityDemo.repositorio;

import com.example.securityDemo.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long>{
    
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Usuario buscarPorEmail(@Param("email") String Email);
}
