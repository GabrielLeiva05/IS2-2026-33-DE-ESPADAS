package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "preventistas")
@Getter
@Setter
@NoArgsConstructor
public class Preventista extends Empleado {

    @Column(length = 100)
    private String zona;
}
