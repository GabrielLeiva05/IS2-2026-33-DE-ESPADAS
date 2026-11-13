package com.ejercicioIntegrador.dulcemayor.dto;

/**
 * Subtotal de ventas. Según el agrupamiento, "departamento" y/o "preventista" vienen informados
 * (por departamento: solo departamento; por preventista: solo preventista; cruzado: ambos).
 */
public record SubtotalVentasDTO(String departamento, String preventista, long cantidadPedidos, double total) {
}
