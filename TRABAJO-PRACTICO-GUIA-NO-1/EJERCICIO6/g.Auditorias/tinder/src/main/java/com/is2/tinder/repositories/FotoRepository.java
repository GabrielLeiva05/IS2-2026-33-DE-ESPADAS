package com.is2.tinder.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.is2.tinder.entities.Foto;

public interface FotoRepository extends JpaRepository<Foto, String> {
    
}
