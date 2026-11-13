package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distribuidores")
@Getter
@Setter
@NoArgsConstructor
public class Distribuidor extends Empleado {

    @Column(length = 80)
    private String vehiculo;

    @Column(length = 10)
    private String patente;
}
