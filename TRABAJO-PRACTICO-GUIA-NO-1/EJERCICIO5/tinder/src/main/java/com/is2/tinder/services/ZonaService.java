package com.is2.tinder.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.is2.tinder.entities.Zona;
import com.is2.tinder.dtos.ZonaDTO;
import com.is2.tinder.errors.ErrorService;
import com.is2.tinder.repositories.ZonaRepository;

@Service
public class ZonaService {
    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    public List<ZonaDTO> listarZona() throws ErrorService {
        try {
            return zonaRepository.findAll().stream().map(ZonaDTO::fromEntity).toList();
        } catch (Exception exception) {
            throw new ErrorService("No se pudieron cargar las zonas");
        }
    }
}