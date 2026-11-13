package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * Cliente minorista (responsable del comercio). Sus datos comerciales viven en PuntoVenta
 * (PuntoVenta 1..* ──> 1 Cliente). Su dirección (heredada de Persona) es la PARTICULAR.
 */
@Entity
@Table(name = "clientes")
@NoArgsConstructor
public class Cliente extends Persona {
}
