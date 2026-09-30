package com.ejercicioIntegrador.tiendaderopa.model;

import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoDocumento;
import com.ejercicioIntegrador.tiendaderopa.enumeraciones.TipoEmpleado;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "empleado")
public class Empleado extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empleado")
    private TipoEmpleado tipoEmpleado;

    @Column(nullable = false)
    private boolean eliminado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    public Empleado(String nombre, String apellido, Date fechaNacimiento, String documento, TipoDocumento tipoDocumento, TipoEmpleado tipoEmpleado, Empresa empresa) {
        super(nombre, apellido, fechaNacimiento, documento, tipoDocumento);
        this.tipoEmpleado = tipoEmpleado;
        this.empresa = empresa;
    }
}