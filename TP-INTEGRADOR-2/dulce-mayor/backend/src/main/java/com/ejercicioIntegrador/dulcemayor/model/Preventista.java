package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Preventista. La "zona" del diagrama original se eliminó (corrección Ola 2): el preventista trabaja con
 * comercios de cualquier departamento de Mendoza. La subclase se conserva porque identifica el rol en las
 * relaciones (Ruta de VISITA, Pedido *──> 1 Preventista).
 * Bases existentes: la columna "zona" queda sin uso; se puede borrar con ALTER TABLE preventistas DROP COLUMN zona.
 */
@Entity
@Table(name = "preventistas")
@Getter
@Setter
@NoArgsConstructor
public class Preventista extends Empleado {
}
