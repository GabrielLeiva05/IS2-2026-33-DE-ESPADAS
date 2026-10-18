package com.tienda.carrito.dto;

/** Datos minimos de un proveedor para dibujarlo en el mapa (se serializa a JSON). */
public record ProveedorUbicacionDTO(String id, String nombre, String direccion, double latitud, double longitud) { }
