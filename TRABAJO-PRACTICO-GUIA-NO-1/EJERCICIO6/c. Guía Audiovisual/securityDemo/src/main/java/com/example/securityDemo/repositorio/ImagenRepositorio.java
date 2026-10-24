package com.example.securityDemo.repositorio;

import com.example.securityDemo.entidades.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenRepositorio extends JpaRepository<Imagen,Long>{
    
}
