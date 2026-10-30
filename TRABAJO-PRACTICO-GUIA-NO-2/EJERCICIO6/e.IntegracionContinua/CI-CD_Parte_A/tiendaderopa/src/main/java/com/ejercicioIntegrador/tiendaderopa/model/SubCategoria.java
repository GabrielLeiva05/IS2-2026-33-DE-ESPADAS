package com.ejercicioIntegrador.tiendaderopa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "subcategorias")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoria {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String nombre;
    private boolean activo = true;

    @ManyToOne
    private Categoria categoria;

}
