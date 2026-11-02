package com.ejercicioIntegrador.dulcemayor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * Cliente minorista (responsable del comercio). Sus datos comerciales viven en PuntoVenta,
 * que se implementará en una etapa posterior (relación Cliente 1 ──> 1..* PuntoVenta).
 */
@Entity
@Table(name = "clientes")
@NoArgsConstructor
public class Cliente extends Persona {
}
