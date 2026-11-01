package com.ejercicioIntegrador.dulcemayor.service;

import com.ejercicioIntegrador.dulcemayor.enumeraciones.Presentacion;
import com.ejercicioIntegrador.dulcemayor.enumeraciones.TipoImagen;
import com.ejercicioIntegrador.dulcemayor.model.Categoria;
import com.ejercicioIntegrador.dulcemayor.model.Imagen;
import com.ejercicioIntegrador.dulcemayor.model.Producto;
import com.ejercicioIntegrador.dulcemayor.model.Stock;
import com.ejercicioIntegrador.dulcemayor.model.SubCategoria;
import com.ejercicioIntegrador.dulcemayor.model.VigenciaPrecio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Objetos de dominio de prueba. */
final class Datos {

    private Datos() {
    }

    static Categoria categoria(String id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }

    static SubCategoria subCategoria(String id, String nombre, Categoria categoria) {
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setId(id);
        subCategoria.setNombre(nombre);
        subCategoria.setCategoria(categoria);
        return subCategoria;
    }

    static Imagen imagen(String id, TipoImagen tipo) {
        Imagen imagen = new Imagen();
        imagen.setId(id);
        imagen.setNombre(id + ".png");
        imagen.setMime("image/png");
        imagen.setContenido(new byte[]{1, 2, 3});
        imagen.setTipoImagen(tipo);
        return imagen;
    }

    static Producto producto(String id, String codigo, String nombre, SubCategoria subCategoria) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion("Descripción de " + nombre);
        producto.setPresentacion(Presentacion.CAJA);
        producto.setUnidadesPorPresentacion(24);
        producto.setSubCategoria(subCategoria);
        producto.setImagenes(new ArrayList<>(List.of(imagen("img-" + id, TipoImagen.PRODUCTO))));
        return producto;
    }

    static VigenciaPrecio vigencia(String id, Producto producto, LocalDate desde, LocalDate hasta, double precio) {
        VigenciaPrecio vigencia = new VigenciaPrecio();
        vigencia.setId(id);
        vigencia.setProducto(producto);
        vigencia.setFechaDesde(desde);
        vigencia.setFechaHasta(hasta);
        vigencia.setPrecio(precio);
        return vigencia;
    }

    static Stock stock(String id, Producto producto, int cantidad, int minimo) {
        Stock stock = new Stock();
        stock.setId(id);
        stock.setProducto(producto);
        stock.setCantidadActual(cantidad);
        stock.setStockMinimo(minimo);
        return stock;
    }
}
