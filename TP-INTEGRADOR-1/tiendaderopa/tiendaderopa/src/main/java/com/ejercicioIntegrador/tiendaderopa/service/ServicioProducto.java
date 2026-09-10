package com.ejercicioIntegrador.tiendaderopa.service;

import com.ejercicioIntegrador.tiendaderopa.model.Producto;

import com.ejercicioIntegrador.tiendaderopa.repository.RepositorioProducto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ServicioProducto {

    @Autowired
    private RepositorioProducto repositorio;
    @Autowired
    private ServicioSubCategoria svcSubCategoria;
    @Autowired
    private ServicioImagen svcImagen;

    @Transactional
    public void crearProducto(String codigo, String nombre, String descripcion, String talle,
                              boolean enOferta, String idImagen, String idSubCategoria) throws Exception {
        validarProducto(codigo, nombre, descripcion, talle, enOferta, idImagen, idSubCategoria);

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setTalle(talle);
        producto.setEnOferta(enOferta);
        producto.setEliminado(false);
        asociarRelaciones(producto, idImagen, idSubCategoria);

        repositorio.save(producto);
    }

    public void validarProducto(String codigo, String nombre, String descripcion, String talle,
                                boolean enOferta, String idImagen, String idSubCategoria) throws Exception {
        if (codigo == null || codigo.isBlank()) throw new Exception("El código es obligatorio");
        if (nombre == null || nombre.isBlank()) throw new Exception("El nombre es obligatorio");
        if (idSubCategoria == null || idSubCategoria.isBlank()) throw new Exception("Debe indicar una subcategoría");
    }

    @Transactional
    public void modificarProducto(String id, String nombre, String descripcion, String talle,
                                  boolean enOferta, String idImagen, String idSubCategoria) throws Exception {
        Producto producto = repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe el producto con id " + id));
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setTalle(talle);
        producto.setEnOferta(enOferta);
        asociarRelaciones(producto, idImagen, idSubCategoria);
        repositorio.save(producto);
    }

    private void asociarRelaciones(Producto producto, String idImagen, String idSubCategoria) throws Exception {
        if (idSubCategoria != null && !idSubCategoria.isBlank()) {
            producto.setSubCategoria(svcSubCategoria.findById(idSubCategoria));
        }
        if (idImagen != null && !idImagen.isBlank()) {
            producto.setImagen(svcImagen.findById(Long.valueOf(idImagen)));
        }
    }

    @Transactional
    public void eliminarProducto(String id) throws Exception {
        Producto producto = repositorio.findById(id)
                .orElseThrow(() -> new Exception("No existe el producto con id " + id));
        producto.setEliminado(true); // baja lógica, nunca deleteById
        repositorio.save(producto);
    }

    public Collection<Producto> listarProducto() {
        return repositorio.findAll();
    }

    public Collection<Producto> listarProductoActivo() {
        return repositorio.findByEliminadoFalse();
    }

    public Producto buscarProductoPorNombre(String nombre) {
        return repositorio.findByNombre(nombre);
    }

    public Producto buscarProductoPorCodigo(String codigo) {
        return repositorio.findByCodigo(codigo);
    }
}