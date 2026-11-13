package com.ejercicioIntegrador.dulcemayor.dto;

import java.math.BigDecimal;
import java.util.List;

/** Producto ofrecido para aclarar un pedido ambiguo ("¿cuál de estos alfajores?"). */
public record OpcionChatDTO(String idProducto, String nombre, String presentacion, BigDecimal precio) {
}
