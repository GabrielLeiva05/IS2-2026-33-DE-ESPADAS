package com.ejercicioIntegrador.dulcemayor.service.presupuesto;

/**
 * Producto tal como lo nombró el cliente ("alfajores de maicena", "chicles") y la cantidad de bultos pedida.
 * La IA no resuelve el producto: lo busca Java en el catálogo.
 */
public record ItemSolicitado(String producto, Integer cantidad) {
}
