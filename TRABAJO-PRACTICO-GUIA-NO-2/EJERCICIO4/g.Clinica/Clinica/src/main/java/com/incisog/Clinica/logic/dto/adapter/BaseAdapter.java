package com.incisog.Clinica.logic.dto.adapter;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseAdapter<E, D> {

    public abstract E toEntity(D dto);
    public abstract D toDTO(E entidad);

    public List<D> listDTO(List<E> entidades) {
        if (entidades == null) {
            return List.of();
        }
        return entidades.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<E> listEntity(List<D> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}