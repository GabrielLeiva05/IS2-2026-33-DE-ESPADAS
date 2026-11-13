package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Empleado administrativo o jefe. Preventista y Distribuidor son subclases. */
@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
public class Empleado extends Persona {

    @Column(nullable = false, length = 20)
    private String legajo;

    @Column(nullable = false)
    private LocalDate fechaIngreso;
}
